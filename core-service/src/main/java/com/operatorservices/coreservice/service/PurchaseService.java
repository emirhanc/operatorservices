package com.operatorservices.coreservice.service;

import com.operatorservices.coreservice.dto.AccountUpdateRequestDto;
import com.operatorservices.coreservice.dto.PurchaseCreateRequestDto;
import com.operatorservices.coreservice.dto.PurchaseDto;
import com.operatorservices.coreservice.dto.PurchaseOrderDto;
import com.operatorservices.coreservice.dto.converter.ModelDtoConverter;
import com.operatorservices.coreservice.exception.EntryNotFoundException;
import com.operatorservices.coreservice.exception.InsufficientFundsException;
import com.operatorservices.coreservice.exception.PurchaseNotPossibleException;
import com.operatorservices.coreservice.model.Account;
import com.operatorservices.coreservice.model.Purchase;
import com.operatorservices.coreservice.model.SubPackage;
import com.operatorservices.coreservice.repository.PurchaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ModelDtoConverter modelDtoConverter;
    private final AccountService accountService;
    private final SubPackageService subPackageService;
    private final KafkaTemplate<String, PurchaseDto> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    public PurchaseService(PurchaseRepository purchaseRepository,
                           ModelDtoConverter modelDtoConverter,
                           AccountService accountService,
                           SubPackageService subPackageService, KafkaTemplate<String, PurchaseDto> kafkaTemplate) {

        this.purchaseRepository = purchaseRepository;
        this.modelDtoConverter = modelDtoConverter;
        this.accountService = accountService;
        this.subPackageService = subPackageService;
        this.kafkaTemplate = kafkaTemplate;
    }

    protected Purchase returnPurchaseById(String id) {
        return purchaseRepository.findById(id)
                .orElseThrow(
                        () -> new EntryNotFoundException("No purchase found with this id: " + id));
    }

    protected BigDecimal returnBalance(BigDecimal accountBalance, Short packagePrice, String packageName){
        if (accountBalance.compareTo(BigDecimal.valueOf(packagePrice)) < 0){
            throw new InsufficientFundsException(
                    "Insufficient account balance to make this purchase: " + packageName
                    + " with the price of " + packagePrice + "." + " Payment Required.");
        }
            return accountBalance.subtract(BigDecimal.valueOf(packagePrice));
    }

    public PurchaseDto getPurchaseById(String purchaseId) {
        return modelDtoConverter.purchaseToPurchaseDto(returnPurchaseById(purchaseId));
    }

    public PurchaseDto createPurchase(PurchaseCreateRequestDto purchaseCreateRequestDto) {

        SubPackage subPackage = subPackageService.returnSubPackageById(purchaseCreateRequestDto.getSubPackageId());

        if (!subPackage.getPurchasable()) {
            throw new PurchaseNotPossibleException ("This package can not be purchased at this moment!");
        }

        Account account = accountService.returnAccountById(purchaseCreateRequestDto.getAccountId());
        BigDecimal newBalance = returnBalance(
                Objects.requireNonNull(account.getAccountBalance()),
                purchaseCreateRequestDto.getPackagePrice(),
                subPackage.getName());

        account.setAccountBalance(newBalance);

        AccountUpdateRequestDto accountUpdateRequestDto = new AccountUpdateRequestDto(
                newBalance,
                Objects.requireNonNull(account.getTariffType())
        );

        accountService.updateAccount(accountUpdateRequestDto, account.getId());

        Purchase purchase = new Purchase(
                LocalDateTime.now(),
                account,
                subPackage,
                purchaseCreateRequestDto.getPackagePrice()
        );

        PurchaseDto purchaseDto = modelDtoConverter.purchaseToPurchaseDto(purchaseRepository.save(purchase));
        kafkaTemplate.send("notification", purchaseDto);

        return purchaseDto;
    }

    @KafkaListener(topics = "purchase-order", errorHandler = "orderErrorHandler", groupId = "group-id")
    @SendTo
    public Object consume(PurchaseOrderDto purchaseOrderDto){
        logger.info("Received order: {}", purchaseOrderDto);
        PurchaseCreateRequestDto purchaseCreateRequest = new PurchaseCreateRequestDto(
                purchaseOrderDto.getAccountId(),
                purchaseOrderDto.getSubPackageId(),
                purchaseOrderDto.getPackagePrice()
        );
        return createPurchase(purchaseCreateRequest);
    }



    public void deletePurchase(String purchaseId) {
        Purchase purchase = returnPurchaseById(purchaseId);
        (Objects.requireNonNull(purchase.getAccount())).setAccountBalance(
                (Objects.requireNonNull(purchase.getAccount().getAccountBalance()))
                        .add(BigDecimal.valueOf(purchase.getPackagePrice())));

        AccountUpdateRequestDto accountUpdateRequestDto = new AccountUpdateRequestDto(
                purchase.getAccount().getAccountBalance(),
                Objects.requireNonNull(purchase.getAccount().getTariffType())
        );
        accountService.updateAccount(accountUpdateRequestDto, purchase.getAccount().getId());
        accountService.syncAccount(purchase);
        subPackageService.syncPackage(purchase);
        purchaseRepository.deleteById(purchaseId);
    }
}
