package com.operatorservices.coreservice.service;

import com.operatorservices.coreservice.dto.AccountCreateRequestDto;
import com.operatorservices.coreservice.dto.AccountDto;
import com.operatorservices.coreservice.dto.AccountUpdateRequestDto;
import com.operatorservices.coreservice.dto.PurchaseDto;
import com.operatorservices.coreservice.dto.converter.ModelDtoConverter;
import com.operatorservices.coreservice.exception.EntryNotFoundException;
import com.operatorservices.coreservice.repository.AccountRepository;
import com.operatorservices.coreservice.model.Account;
import com.operatorservices.coreservice.model.Customer;
import com.operatorservices.coreservice.model.Purchase;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final ModelDtoConverter modelDtoConverter;
    private final CustomerService customerService;


    public AccountService(
            AccountRepository accountRepository,
            ModelDtoConverter modelDtoConverter,
            CustomerService customerService) {

        this.accountRepository = accountRepository;
        this.modelDtoConverter = modelDtoConverter;
        this.customerService = customerService;
    }


    protected Account returnAccountById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(
                        () -> new EntryNotFoundException("No account found with this id: " + id));
    }

    public AccountDto getAccountById(String accountId) {
        return modelDtoConverter.accountToAccountDto(returnAccountById(accountId));
    }

    public List<AccountDto> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(modelDtoConverter::accountToAccountDto)
                .collect(Collectors.toList());
    }

    public List<PurchaseDto> getAllPurchases(String accountId){
        Account account = returnAccountById(accountId);
        return account.getPurchases()
                .stream()
                .map(modelDtoConverter::purchaseToPurchaseDto)
                .collect(Collectors.toList());
    }
    public AccountDto createAccount(AccountCreateRequestDto accountCreateRequestDto) {

        Customer customer = customerService.returnCustomerById(accountCreateRequestDto.getCustomerId());

        Account account = new Account(
                LocalDateTime.now(),
                customer,
                accountCreateRequestDto.getAccountBalance(),
                accountCreateRequestDto.getTariffType()
        );
        return modelDtoConverter.accountToAccountDto(accountRepository.save(account));
    }

    public AccountDto updateAccount(AccountUpdateRequestDto accountUpdateDto, String accountId) {

        return accountRepository.findById(accountId)
                .map(account -> {
                    account.setAccountBalance(accountUpdateDto.getAccountBalance());
                    account.setTariffType(accountUpdateDto.getTariffType());
                    return modelDtoConverter.accountToAccountDto(accountRepository.save(account));
                })
                .orElseThrow(
                        () -> new EntryNotFoundException("No account found with this id: " + accountId));
    }

    public void syncAccount(Purchase purchase){
       accountRepository.findById(Objects.requireNonNull(purchase.getAccount()).getId())
               .map(account -> {
                   account.getPurchases().remove(purchase);
                   return accountRepository.save(account);
               });
    }

    public void deleteAccount(String accountId) {
        if (accountRepository.existsById(accountId)) {
            accountRepository.deleteById(accountId);
        } else {
            throw new EntryNotFoundException("No account found with this id: " + accountId);
        }
    }
}