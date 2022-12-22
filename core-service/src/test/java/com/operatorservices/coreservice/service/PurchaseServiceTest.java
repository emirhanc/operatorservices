package com.operatorservices.coreservice.service;

import com.operatorservices.coreservice.TestSupport;
import com.operatorservices.coreservice.dto.AccountUpdateRequestDto;
import com.operatorservices.coreservice.dto.PackageDto;
import com.operatorservices.coreservice.dto.PurchaseCreateRequestDto;
import com.operatorservices.coreservice.dto.PurchaseDto;
import com.operatorservices.coreservice.dto.converter.ModelDtoConverter;
import com.operatorservices.coreservice.exception.EntryNotFoundException;
import com.operatorservices.coreservice.exception.InsufficientFundsException;
import com.operatorservices.coreservice.exception.PurchaseNotPossibleException;
import com.operatorservices.coreservice.model.Account;
import com.operatorservices.coreservice.model.Purchase;
import com.operatorservices.coreservice.model.SubPackage;
import com.operatorservices.coreservice.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PurchaseServiceTest extends TestSupport {

    private PurchaseRepository purchaseRepository;
    private ModelDtoConverter modelDtoConverter;
    private AccountService accountService;
    private SubPackageService subPackageService;
    private PurchaseService purchaseService;
    private KafkaTemplate kafkaTemplate;


    @BeforeEach
    void setUp() {

        purchaseRepository = mock(PurchaseRepository.class);
        accountService = mock(AccountService.class);
        modelDtoConverter = mock(ModelDtoConverter.class);
        subPackageService = mock(SubPackageService.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        purchaseService = new PurchaseService(
                purchaseRepository,
                modelDtoConverter,
                accountService,
                subPackageService, kafkaTemplate);
    }

    @DisplayName("getPurchaseById with Valid Id Test")
    @Test
    void whenGetPurchaseById_withAValidId_itShouldReturnPurchaseDto() {

        Account account = newAccount("accountId", "customerId", 100L, Set.of());
        Purchase purchase = newPurchase(account, "purchaseId", 1L);
        SubPackage subPackage = newSubPackage(1L);
        PackageDto packageDto = newPackageDto(subPackage);
        PurchaseDto purchaseDto = newPurchaseDto(purchase, packageDto);

        when(purchaseRepository.findById("purchaseId")).thenReturn(Optional.of(purchase));
        when(modelDtoConverter.purchaseToPurchaseDto(purchase)).thenReturn(purchaseDto);

        PurchaseDto test = purchaseService.getPurchaseById("purchaseId");

        assertEquals(test, purchaseDto);

        verify(purchaseRepository).findById("purchaseId");
        verify(modelDtoConverter).purchaseToPurchaseDto(purchase);
    }

    @DisplayName("getPurchaseById with Invalid Id Test")
    @Test
    void whenGetPurchaseById_withAnInvalidId_itShouldThrowEntryNotFoundException() {

        when(purchaseRepository.findById("invalidId")).thenReturn(Optional.empty());

        assertThrows(EntryNotFoundException.class,
                ()->purchaseService.getPurchaseById("invalidId"));

        verify(purchaseRepository).findById("invalidId");
        verifyNoInteractions(modelDtoConverter);
    }

    @DisplayName("createPurchase with Valid Request Test")
    @Test
    void whenCreatePurchase_withValidRequest_itShouldReturnPurchaseDto() {

        Account account = newAccount("accountId", "customerId", 200L, Set.of());
        SubPackage subPackage = newSubPackage(1L);
        Purchase purchase = newPurchase(account, "purchaseId", 1L);
        PurchaseDto purchaseDto = newPurchaseDto(purchase, newPackageDto(subPackage));
        PurchaseCreateRequestDto purchaseCreateRequestDto = new PurchaseCreateRequestDto(
                "accountId", 1L, (short) 100);

        when(accountService.returnAccountById(purchaseCreateRequestDto.getAccountId()))
                .thenReturn(purchase.getAccount());

        when(subPackageService.returnSubPackageById(purchaseCreateRequestDto.getSubPackageId()))
                .thenReturn(purchase.getSubPackage());

        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
        when(modelDtoConverter.purchaseToPurchaseDto(purchase)).thenReturn(purchaseDto);

        PurchaseDto test = purchaseService.createPurchase(purchaseCreateRequestDto);

        assertEquals(test, purchaseDto);

        verify(accountService).returnAccountById("accountId");
        verify(subPackageService).returnSubPackageById(1L);
        verify(purchaseRepository).save(any(Purchase.class));
        verify(modelDtoConverter).purchaseToPurchaseDto(purchase);
    }

    @DisplayName("createPurchase with Invalid Request Test")
    @Test
    void whenCreatePurchase_withValidRequest_itShouldThrowInsufficientFundsException() {
        Account account = newAccount("id", "id", 50L, Set.of());
        Purchase purchase = newPurchase(account, "id", 1L); //Price is set to 100

        assertThrows(InsufficientFundsException.class,
                () -> purchaseService.returnBalance(
                        Objects.requireNonNull(account.getAccountBalance()),
                        purchase.getPackagePrice(),
                        Objects.requireNonNull(purchase.getSubPackage()).getName())
        );

        verifyNoInteractions(accountService);
        verifyNoInteractions(purchaseRepository);
        verifyNoInteractions(modelDtoConverter);
    }

    @DisplayName("createPurchase with Forbidden Package Test")
    @Test
    void whenCreatePurchase_withForbiddenPackage_itShouldThrowPurchaseNotPossibleException() {
        Account account = newAccount("id", "id", 200L, Set.of());
        Purchase purchase = newForbiddenPurchase(account, "id", 1L);
        PurchaseCreateRequestDto purchaseCreateRequestDto =
                newPurchaseCreateRequestDto(account.getId(), Objects.requireNonNull(purchase.getSubPackage()).getId());

        when(subPackageService.returnSubPackageById(purchaseCreateRequestDto.getSubPackageId()))
                .thenReturn(purchase.getSubPackage());

        assertThrows(PurchaseNotPossibleException.class,
                () -> purchaseService.createPurchase(purchaseCreateRequestDto));

        verifyNoInteractions(accountService);
        verifyNoInteractions(purchaseRepository);
        verifyNoInteractions(modelDtoConverter);
    }

    @DisplayName("deletePurchase with Valid Id Test")
    @Test
    void whenDeletePurchaseCalled_withAValidId_itShouldDeletePurchaseAndUpdateAccount() {
        Account account = newAccount("id", "id", 200L, Set.of());
        Purchase purchase = newPurchase(account, "validId", 1L);

        AccountUpdateRequestDto accountUpdateRequestDto = new AccountUpdateRequestDto(
                Objects.requireNonNull(account.getAccountBalance()).add(BigDecimal.valueOf(purchase.getPackagePrice())),
                Objects.requireNonNull(Objects.requireNonNull(purchase.getAccount()).getTariffType())
        );

        when(purchaseRepository.findById("validId")).thenReturn(Optional.of(purchase));

        purchaseService.deletePurchase("validId");

        verify(purchaseRepository).findById("validId");
        verify(accountService).updateAccount(accountUpdateRequestDto, account.getId());
        verify(accountService).syncAccount(purchase);
        verify(subPackageService).syncPackage(purchase);
        verify(purchaseRepository).deleteById("validId");
    }
}