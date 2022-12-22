package com.operatorservices.coreservice;

import com.operatorservices.coreservice.dto.*;
import com.operatorservices.coreservice.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public abstract class TestSupport {

    public Customer newCustomer(String id) {
        return new Customer(
                id,
                LocalDateTime.now(),
                "Name",
                "Surname",
                "test@mail.com",
                "password",
                new LinkedHashSet<>()
        );
    }

    public CustomerGetDto newCustomerGetDto(Customer customer){
        return new CustomerGetDto(
                customer.getId(),
                customer.getCreationDate(),
                customer.getName(),
                customer.getSurname(),
                customer.getEmail(),
                Set.of()
        );
    }

    public CustomerDto newCustomerDto(Customer customer){
        return new CustomerDto(
                customer.getId(),
                customer.getCreationDate(),
                customer.getName(),
                customer.getSurname(),
                customer.getEmail(),
                customer.getPassword(),
                Set.of()
        );
    }

    public CustomerCreateRequestDto newCustomerCreateRequestDto(Customer customer){
        return new CustomerCreateRequestDto(
                customer.getName(),
                customer.getSurname(),
                customer.getEmail(),
                customer.getPassword()
        );
    }

    public CustomerUpdateRequestDto newCustomerUpdateRequestDto(Customer customer){
        return new CustomerUpdateRequestDto(
                customer.getEmail(),
                customer.getPassword());
    }

    public Account newAccount(String id, String customerId, Long accountBalance, Set<Purchase> purchases){
        return new Account(
                id,
                newCustomer(customerId),
                LocalDateTime.now(),
                BigDecimal.valueOf(accountBalance),
                TariffType.PREMIUM,
                purchases);
    }

    public AccountRequestGetCustomerDto newAccountRequestGetCustomerDto (Customer customer) {
        return new  AccountRequestGetCustomerDto(
                customer.getId(),
                customer.getName(),
                customer.getSurname(),
                customer.getEmail());
    }


    public CustomerRequestGetAccountDto newCustomerRequestGetAccountDto (Account account){
        return new CustomerRequestGetAccountDto(
                account.getId(),
                Objects.requireNonNull(account.getCreationDate()),
                Objects.requireNonNull(account.getTariffType()),
                Objects.requireNonNull(account.getAccountBalance()),
                Set.of());
    }

    public AccountDto newAccountDto(Account account){
        return new AccountDto(
                account.getId(),
                Objects.requireNonNull(account.getCreationDate()),
                newAccountRequestGetCustomerDto(Objects.requireNonNull(account.getCustomer())),
                Objects.requireNonNull(account.getTariffType()),
                Objects.requireNonNull(account.getAccountBalance()),
                Set.of());
    }

    public SubPackage newSubPackage(Long subPackageId) {
        return new SubPackage(
                subPackageId,
                "NewPackage",
                PackageType.COMBO,
                12,
                true,
                Set.of());
    }

    public SubPackage newSubPackage(Long subPackageId, Set<Purchase> purchases) {
        return new SubPackage(
                subPackageId,
                "NewPackage",
                PackageType.COMBO,
                12,
                true,
                purchases);
    }

    public SubPackage newForbiddenSubPackage(Long subPackageId) {
        return new SubPackage(
                subPackageId,
                "NewPackage",
                PackageType.COMBO,
                12,
                false,
                Set.of());
    }

    public Purchase newPurchase(Account account, String purchaseId, Long subPackageId){
        return new Purchase(
                purchaseId,
                LocalDateTime.now(),
                (short) 100,
                account,
                newSubPackage(subPackageId));
    }

    public Purchase newForbiddenPurchase(Account account, String purchaseId, Long subPackageId){
        return new Purchase(
                purchaseId,
                LocalDateTime.now(),
                (short) 100,
                account,
                newForbiddenSubPackage(subPackageId));
    }

    public PackageDto newPackageDto(SubPackage subPackage){
        return new PackageDto(
                subPackage.getId(),
                subPackage.getName(),
                subPackage.getPackageType(),
                subPackage.getDuration(),
                subPackage.getPurchasable());
    }

    public PurchaseDto newPurchaseDto(Purchase purchase, PackageDto packageDto){
        return new PurchaseDto(
                purchase.getId(),
                Objects.requireNonNull(purchase.getPurchaseDate()),
                packageDto);
    }

    public PurchaseCreateRequestDto newPurchaseCreateRequestDto(String accountId, Long subPackageId){
        return new PurchaseCreateRequestDto(
                accountId,
                subPackageId,
                (short) 50);
    }

    public GetAccountsByPackageDto newGetAccountsByPackageDto(Account account){
        return new GetAccountsByPackageDto(
                account.getId(),
                newAccountRequestGetCustomerDto(Objects.requireNonNull(account.getCustomer())));
    }

    public PackageRequestDto newPackageRequestDto(SubPackage subPackage){
        return new PackageRequestDto(
                subPackage.getName(),
                subPackage.getPackageType(),
                subPackage.getPurchasable(),
                subPackage.getDuration());
    }
}
