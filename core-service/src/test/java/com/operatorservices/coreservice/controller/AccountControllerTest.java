package com.operatorservices.coreservice.controller;

import com.operatorservices.coreservice.IntegrationSetup;
import com.operatorservices.coreservice.exception.EntryNotFoundException;
import com.operatorservices.coreservice.model.Account;
import com.operatorservices.coreservice.model.Customer;
import com.operatorservices.coreservice.model.TariffType;
import com.operatorservices.coreservice.repository.AccountRepository;
import com.operatorservices.coreservice.service.CustomerService;
import com.operatorservices.coreservice.service.SubPackageService;
import com.operatorservices.coreservice.dto.AccountCreateRequestDto;
import com.operatorservices.coreservice.dto.AccountUpdateRequestDto;
import com.operatorservices.coreservice.dto.CustomerCreateRequestDto;
import com.operatorservices.coreservice.dto.PackageRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class AccountControllerTest extends IntegrationSetup {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SubPackageService packageService;

    @DisplayName("getAccountById with Valid Id Test")
    @Test
    void getAccountById_whenIdIsValid_thenReturnAccount() throws Exception{

        Customer dummyCusto = newCustomer("dummyCusto");
        CustomerCreateRequestDto customerCreateRequestDto =
                newCustomerCreateRequestDto(Objects.requireNonNull(dummyCusto));

        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        Account dummyAccount = newAccount("dummy", customerId, 100L, Set.of());

        Account account = accountRepository.save(dummyAccount);

        this.mockMvc.perform(get("/v1/accounts/{accountId}/", account.getId()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id", is(account.getId())))
                .andExpect(jsonPath("$.creationDate", notNullValue()))
                .andExpect(jsonPath("$.customer.length()", is(4)))
                .andExpect(jsonPath("$.tariffType", is(String.valueOf(account.getTariffType()))))
                .andExpect(jsonPath("$.accountBalance", is(100.0)))
                .andExpect(jsonPath("$.purchases", hasSize(0)))

                .andExpect(jsonPath("_links.self.href",
                        is("http://localhost/v1/accounts/" + account.getId())))

                .andExpect(jsonPath("_links.collection.href",
                        is("http://localhost/v1/accounts")))

                .andExpect(jsonPath("_links.purchases.href",
                        is("http://localhost/v1/accounts/" + account.getId() + "/purchases")));
    }

    @DisplayName("getAccountById with Invalid Id Test")
    @Test
    void getAccountById_whenIdIsInvalid_thenReturn404andException() throws Exception{

        this.mockMvc.perform(get("/v1/accounts/{accountId}", "404"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))
                .andExpect(result -> assertEquals(
                        "No account found with this id: 404",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @DisplayName("getPurchasesByAccountId with Valid Id Test")
    @Test
    void getPurchasesByAccountId_whenIdIsValid_thenReturnListOfPurchases() throws Exception {

        Customer dummyCusto = newCustomer("dummyCusto");
        CustomerCreateRequestDto customerCreateRequestDto =
                newCustomerCreateRequestDto(Objects.requireNonNull(dummyCusto));

        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        Account dummyAccount = newAccount("dummy", customerId, 100L, Set.of());

        Account account = accountRepository.save(dummyAccount);

        PackageRequestDto packageRequestDto = newPackageRequestDto(newSubPackage(1L, Set.of()));
        Long packageId = packageService.createPackage(packageRequestDto).getId();

        account.getPurchases().add(newPurchase(account, "purchase1", packageId));
        account.getPurchases().add(newPurchase(account, "purchase2", packageId));

        String accountId = accountRepository.save(account).getId();

        this.mockMvc.perform(get("/v1/accounts/{accountId}/purchases", accountId))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.size()", is(2)));
    }

    @DisplayName("getPurchasesByAccountId with Invalid Id Test")
    @Test
    void getPurchasesByAccountId_whenIdIsInvalid_thenReturn404andException() throws Exception {

        this.mockMvc.perform(get("/v1/accounts/{accountId}/purchases", "404"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))
                .andExpect(result -> assertEquals(
                        "No account found with this id: 404",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @DisplayName("getAllAccounts when Account Exists Test")
    @Test
    void getAllAccounts_whenAccountExist_thenReturnListOfAccounts() throws Exception {

        Customer dummyCusto = newCustomer("dummyCusto");
        CustomerCreateRequestDto customerCreateRequestDto =
                newCustomerCreateRequestDto(Objects.requireNonNull(dummyCusto));

        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        accountRepository.save(newAccount("account1", customerId, 100L, Set.of()));
        accountRepository.save(newAccount("account2", customerId, 100L, Set.of()));

        this.mockMvc.perform(get("/v1/accounts"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.size()", is(2)));
    }

    @DisplayName("createAccount with Valid Id Test")
    @Test
    void createAccount_whenCustomerIdIsValid_thenCreateAccount() throws Exception {

        Customer dummyCusto = newCustomer("dummyCusto");
        CustomerCreateRequestDto customerCreateRequestDto =
                newCustomerCreateRequestDto(Objects.requireNonNull(dummyCusto));

        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        AccountCreateRequestDto body = new AccountCreateRequestDto(
                customerId,
                BigDecimal.valueOf(100L),
                TariffType.STANDARD
        );

        this.mockMvc.perform(post("/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.creationDate", notNullValue()))
                .andExpect(jsonPath("$.customer.length()", is(4)))
                .andExpect(jsonPath("$.tariffType", is("STANDARD")))
                .andExpect(jsonPath("$.accountBalance", is(100)))
                .andExpect(jsonPath("$.purchases", hasSize(0)))

                .andExpect(jsonPath("_links.self.href", notNullValue()))

                .andExpect(jsonPath("_links.collection.href",
                        is("http://localhost/v1/accounts")))

                .andExpect(jsonPath("_links.purchases.href", notNullValue()));
    }

    @DisplayName("createAccount with Invalid Id Test")
    @Test
    void createAccount_whenCustomerIdIsInvalid_thenReturn404andException() throws Exception {

        AccountCreateRequestDto body = new AccountCreateRequestDto(
                "InvalidId",
                BigDecimal.valueOf(100L),
                TariffType.STANDARD
        );

        this.mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))
                .andExpect(result -> assertEquals(
                        "No customer found with this id: InvalidId",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @DisplayName("createAccount with Blank Id Test")
    @Test
    void createAccount_whenCustomerIdIsBlank_thenReturn400andException() throws Exception {

        AccountCreateRequestDto body = new AccountCreateRequestDto(
                "",
                BigDecimal.valueOf(100L),
                TariffType.STANDARD
        );

        this.mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("createAccount with Invalid AccountBalance Test")
    @Test
    void createAccount_whenCustomerAccountBalanceIsInvalid_thenReturn400andException() throws Exception {

        Customer dummyCusto = newCustomer("dummyCusto");
        CustomerCreateRequestDto customerCreateRequestDto =
                newCustomerCreateRequestDto(Objects.requireNonNull(dummyCusto));

        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        AccountCreateRequestDto body = new AccountCreateRequestDto(
                customerId,
                BigDecimal.valueOf(-400),
                TariffType.STANDARD
        );

        this.mockMvc.perform(post("/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("updateAccount test with Valid Id Test")
    @Test
    void updateAccount_whenIdIsValid_thenReturnAccount() throws Exception {

        Customer dummyCusto = newCustomer("dummyCusto");
        CustomerCreateRequestDto customerCreateRequestDto =
                newCustomerCreateRequestDto(Objects.requireNonNull(dummyCusto));

        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        Account dummyAccount = newAccount("dummy", customerId, 100L, Set.of());

        String accountId = accountRepository.save(dummyAccount).getId();

        AccountUpdateRequestDto body = new AccountUpdateRequestDto(
                BigDecimal.valueOf(200),
                TariffType.PREMIUM
        );

        this.mockMvc.perform(patch("/v1/accounts/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(body)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id", is(accountId)))
                .andExpect(jsonPath("$.creationDate", notNullValue()))
                .andExpect(jsonPath("$.customer.length()", is(4)))
                .andExpect(jsonPath("$.tariffType", is("PREMIUM")))
                .andExpect(jsonPath("$.accountBalance", is(200)))
                .andExpect(jsonPath("$.purchases", hasSize(0)))

                .andExpect(jsonPath("_links.self.href",
                        is("http://localhost/v1/accounts/" + accountId)))

                .andExpect(jsonPath("_links.collection.href",
                        is("http://localhost/v1/accounts")))

                .andExpect(jsonPath("_links.purchases.href",
                        is("http://localhost/v1/accounts/" + accountId + "/purchases")));
    }

    @DisplayName("updateAccount test with Invalid Id Test")
    @Test
    void updateAccount_whenIdIsValid_thenReturn404andException() throws Exception {

        AccountUpdateRequestDto body = new AccountUpdateRequestDto(
                BigDecimal.valueOf(200),
                TariffType.ECONOMY
        );

        this.mockMvc.perform(patch("/v1/accounts/{accountId}", "404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))
                .andExpect(result -> assertEquals(
                        "No account found with this id: 404",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @DisplayName("updateAccount test with Invalid AccountBalance Test")
    @Test
    void updateAccount_whenAccountBalanceIsInvalid_thenReturn400andException() throws Exception {

        Customer dummyCusto = newCustomer("dummyCusto");
        CustomerCreateRequestDto customerCreateRequestDto =
                newCustomerCreateRequestDto(Objects.requireNonNull(dummyCusto));

        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        Account dummyAccount = newAccount("dummy", customerId, 100L, Set.of());
        String accountId = accountRepository.save(dummyAccount).getId();

        AccountUpdateRequestDto body = new AccountUpdateRequestDto(
                BigDecimal.valueOf(-400),
                TariffType.ECONOMY
        );

        this.mockMvc.perform(patch("/v1/accounts/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("deleteAccountById with Valid Id Test")
    @Test
    void deleteAccountById_whenIdIsValid_thenDeleteAccount() throws Exception {

        Customer dummyCusto = newCustomer("dummyCusto");
        CustomerCreateRequestDto customerCreateRequestDto =
                newCustomerCreateRequestDto(Objects.requireNonNull(dummyCusto));

        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        Account dummyAccount = newAccount("dummy", customerId, 100L, Set.of());
        String accountId = accountRepository.save(dummyAccount).getId();

        this.mockMvc.perform(delete("/v1/accounts/{accountId}", accountId))
                .andExpect(status().isNoContent());
    }

    @DisplayName("deleteAccountById with Invalid Id Test")
    @Test
    void deleteAccountById_whenIdIsInvalid_thenDeleteAccount() throws Exception {

        this.mockMvc.perform(delete("/v1/accounts/{accountId}", "404"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))
                .andExpect(result -> assertEquals(
                        "No account found with this id: 404",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }
}