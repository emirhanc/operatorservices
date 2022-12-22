package com.operatorservices.coreservice.service;

import com.operatorservices.coreservice.TestSupport;
import com.operatorservices.coreservice.dto.converter.ModelDtoConverter;
import com.operatorservices.coreservice.exception.EntryNotFoundException;
import com.operatorservices.coreservice.repository.AccountRepository;
import com.operatorservices.coreservice.dto.AccountCreateRequestDto;
import com.operatorservices.coreservice.dto.AccountDto;
import com.operatorservices.coreservice.dto.AccountUpdateRequestDto;
import com.operatorservices.coreservice.dto.PurchaseDto;
import com.operatorservices.coreservice.model.Account;
import com.operatorservices.coreservice.model.Purchase;
import com.operatorservices.coreservice.model.TariffType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest extends TestSupport {

    private AccountService accountService;
    private AccountRepository accountRepository;
    private ModelDtoConverter modelDtoConverter;
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        modelDtoConverter = mock(ModelDtoConverter.class);
        customerService = mock(CustomerService.class);
        accountService = new AccountService(accountRepository, modelDtoConverter, customerService);
    }

    @DisplayName("getAccountById with Valid Id Test")
    @Test
    void whenGetAccountByIdCalled_withAValidId_itShouldReturnAccountDto() {

        Account account = newAccount("accountId", "customerId", 100L, Set.of());
        AccountDto accountDto = newAccountDto(account);

        when(accountRepository.findById("accountId")).thenReturn(Optional.of(account));
        when(modelDtoConverter.accountToAccountDto(account)).thenReturn(accountDto);

        AccountDto test = accountService.getAccountById("accountId");

        assertEquals(test, accountDto);

        verify(accountRepository).findById("accountId");
        verify(modelDtoConverter).accountToAccountDto(account);
    }

    @DisplayName("getAccountById with Invalid Id Test")
    @Test
    void whenGetAccountByIdCalled_withAnInValidId_itShouldThrowEntryNotFoundException() {

        when(accountRepository.findById("invalidId")).thenReturn(Optional.empty());

        assertThrows(EntryNotFoundException.class,
                () -> accountService.getAccountById("invalidId"));

        verify(accountRepository).findById("invalidId");
        verifyNoInteractions(modelDtoConverter);
    }

    @DisplayName("getAllAccounts test")
    @Test
    void whenGetAllAccountsCalled_itShouldReturnAListOfAccountDto() {

        Account account1 = newAccount("account-1","customer-1", 100L, Set.of());
        Account account2 = newAccount("account-1","customer-2", 100L, Set.of());

        when(accountRepository.findAll()).thenReturn(List.of(account1, account2));
        when(modelDtoConverter.accountToAccountDto(account1)).thenReturn(newAccountDto(account1));
        when(modelDtoConverter.accountToAccountDto(account2)).thenReturn(newAccountDto(account2));

        List<AccountDto> test = accountService.getAllAccounts();

        assertEquals(test, List.of(newAccountDto(account1),newAccountDto(account2)));

        verify(accountRepository).findAll();
        verify(modelDtoConverter).accountToAccountDto(account1);
        verify(modelDtoConverter).accountToAccountDto(account1);
    }

    @DisplayName("getAllPurchases with Valid Id Test")
    @Test
    void whenGetAllPurchasesCalled_withAValidId_itShouldReturnAListOfPurchaseDto() {

        Account dummy = newAccount("accountId", "customerId", 100L, Set.of());
        Purchase purchase1 = newPurchase(dummy, "purchase1-Id", 1L);
        Purchase purchase2 = newPurchase(dummy, "purchase2-Id", 2L);
        Set<Purchase> purchases = Set.of(purchase1, purchase2);
        Account account = newAccount("accountId", "customerId", 100L, purchases);

        when(accountRepository.findById("accountId")).thenReturn(Optional.of(account));

        when(modelDtoConverter.purchaseToPurchaseDto(purchase1))
                .thenReturn(newPurchaseDto(purchase1, newPackageDto(newSubPackage(1L))));

        when(modelDtoConverter.purchaseToPurchaseDto(purchase2))
                .thenReturn(newPurchaseDto(purchase2, newPackageDto(newSubPackage(2L))));

        List<PurchaseDto> test = accountService.getAllPurchases("accountId");
        test.sort(Comparator.comparing(PurchaseDto::getId));

        assertEquals(test, List.of(
                newPurchaseDto(purchase1, newPackageDto(newSubPackage(1L))),
                newPurchaseDto(purchase2, newPackageDto(newSubPackage(2L)))));

        verify(accountRepository).findById("accountId");
        verify(modelDtoConverter).purchaseToPurchaseDto(purchase1);
        verify(modelDtoConverter).purchaseToPurchaseDto(purchase2);
    }

    @DisplayName("crateAccount with Valid Request Test")
    @Test
    void whenCreateAccountCalled_withValidRequest_itShouldReturnAccountDto(){

        Account account = newAccount("accountId", "customerId", 100L, Set.of());
        AccountDto accountDto = newAccountDto(account);

        AccountCreateRequestDto accountCreateRequestDto = new AccountCreateRequestDto(
                Objects.requireNonNull(account.getCustomer()).getId(),
                Objects.requireNonNull(account.getAccountBalance()),
                Objects.requireNonNull(account.getTariffType()));

        when(customerService.returnCustomerById(accountCreateRequestDto.getCustomerId()))
                .thenReturn(account.getCustomer());

        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(modelDtoConverter.accountToAccountDto(account)).thenReturn(accountDto);

        AccountDto test = accountService.createAccount(accountCreateRequestDto);

        assertEquals(test, accountDto);

        verify(customerService).returnCustomerById(accountCreateRequestDto.getCustomerId());
        verify(accountRepository).save(any(Account.class));
        verify(modelDtoConverter).accountToAccountDto(account);
    }

    @DisplayName("updateAccount with Valid Id Test")
    @Test
    void whenUpdateAccountCalled_withAValidId_itShouldReturnAccountDto(){

        Account account = newAccount("accountId", "customerId", 100L, Set.of());
        AccountDto accountDto = newAccountDto(account);

        AccountUpdateRequestDto accountUpdateRequestDto = new AccountUpdateRequestDto(
          BigDecimal.valueOf(100L),
          TariffType.STANDARD);

        when(accountRepository.findById("accountId")).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(modelDtoConverter.accountToAccountDto(account)).thenReturn(accountDto);

        AccountDto test = accountService.updateAccount(accountUpdateRequestDto, "accountId");

        assertEquals(test, accountDto);

        verify(accountRepository).findById("accountId");
        verify(accountRepository).save(account);
        verify(modelDtoConverter).accountToAccountDto(account);
    }

    @DisplayName("syncAccount Test")
    @Test
    void whenSyncAccountCalled_itShouldUpdateAccount() {
        Account dummy = newAccount("accountId", "customerId",100L, new HashSet<>());
        Purchase purchase = newPurchase(dummy, "purchaseId", 1L);
        Account account = newAccount(
                "accountId",
                "customerId",
                100L,
                new HashSet<>(Collections.singleton(purchase)));

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        accountService.syncAccount(purchase);

        verify(accountRepository).findById(account.getId());
        verify(accountRepository).save(any(Account.class));
    }

    @DisplayName("deleteAccount with Invalid Id Test")
    @Test
    void whenDeleteAccountCalled_withAnInvalidId_itShouldThrowEntryNotFoundException() {
        when(accountRepository.existsById("invalidId")).thenReturn(false);

        assertThrows(EntryNotFoundException.class,
                () -> accountService.deleteAccount("invalidId"));

        verify(accountRepository).existsById("invalidId");
        verify(accountRepository, times(0)).deleteById("invalidId");
    }

    @DisplayName("deleteAccount with Valid Id Test")
    @Test
    void whenDeleteAccountCalled_withAValidId_itShouldDeleteAccount() {
        when(accountRepository.existsById("id")).thenReturn(true);

        accountService.deleteAccount("id");

        verify(accountRepository).existsById("id");
        verify(accountRepository).deleteById("id");
    }
}