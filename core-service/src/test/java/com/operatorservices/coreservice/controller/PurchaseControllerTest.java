package com.operatorservices.coreservice.controller;

import com.operatorservices.coreservice.IntegrationSetup;
import com.operatorservices.coreservice.exception.EntryNotFoundException;
import com.operatorservices.coreservice.exception.PurchaseNotPossibleException;
import com.operatorservices.coreservice.model.Customer;
import com.operatorservices.coreservice.model.SubPackage;
import com.operatorservices.coreservice.model.TariffType;
import com.operatorservices.coreservice.service.AccountService;
import com.operatorservices.coreservice.service.CustomerService;
import com.operatorservices.coreservice.service.PurchaseService;
import com.operatorservices.coreservice.service.SubPackageService;
import com.operatorservices.coreservice.dto.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.util.Objects;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PurchaseControllerTest extends IntegrationSetup {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private SubPackageService subPackageService;


    @DisplayName("getPurchaseById with Valid Id Test")
    @Test
    void getPurchaseById_whenIdIsValid_thenReturnPurchase() throws Exception {

        Customer dummyCusto = newCustomer("dummy");
        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(dummyCusto);
        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        AccountCreateRequestDto accountCreateRequestDto = new AccountCreateRequestDto(
                customerId,
                BigDecimal.valueOf(100),
                TariffType.PREMIUM
        );
        String accountId = accountService.createAccount(accountCreateRequestDto).getId();

        SubPackage dummyPackage = newSubPackage(1L);
        PackageRequestDto packageRequestDto = newPackageRequestDto(dummyPackage);
        Long packageId = subPackageService.createPackage(packageRequestDto).getId();

        PurchaseCreateRequestDto purchaseCreateRequestDto = new PurchaseCreateRequestDto(
                accountId,
                packageId,
                (short) 25
        );

       PurchaseDto purchaseDto = purchaseService.createPurchase(purchaseCreateRequestDto);


        this.mockMvc.perform(get("/v1/purchases/{purchaseId}", purchaseDto.getId()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id", is(purchaseDto.getId())))
                .andExpect(jsonPath("$.purchaseDate", notNullValue()))
                .andExpect(jsonPath("$.subPackage.length()", is(5)))

                .andExpect(jsonPath("_links.self.href",
                        is("http://localhost/v1/purchases/" + purchaseDto.getId())));
    }

    @DisplayName("getPurchaseById with Invalid Id Test")
    @Test
    void getPurchaseById_whenIdIsInvalid_thenReturn404andException() throws Exception {

        this.mockMvc.perform(get("/v1/purchases/{purchaseId}", "404"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))
                .andExpect(result -> assertEquals(
                        "No purchase found with this id: 404",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    /*
    @DisplayName("createPurchase with Valid Request Test")
    @Test
    void createPurchase_whenRequestIsValid_thenCreatePurchase() throws Exception {

        Customer dummyCusto = newCustomer("dummy");
        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(dummyCusto);
        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        AccountCreateRequestDto accountCreateRequestDto = new AccountCreateRequestDto(
                customerId,
                BigDecimal.valueOf(100),
                TariffType.PREMIUM
        );
        String accountId = accountService.createAccount(accountCreateRequestDto).getId();

        SubPackage dummyPackage = newSubPackage(1L);
        PackageRequestDto packageRequestDto = newPackageRequestDto(dummyPackage);
        Long packageId = subPackageService.createPackage(packageRequestDto).getId();

        PurchaseCreateRequestDto purchaseCreateRequestDto = new PurchaseCreateRequestDto(
                accountId,
                packageId,
                (short) 25
        );

        this.mockMvc.perform(post("/v1/purchases/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(purchaseCreateRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.purchaseDate", notNullValue()))
                .andExpect(jsonPath("$.subPackage.length()", is(5)))

                .andExpect(jsonPath("_links.self.href",
                        notNullValue()));
    }

    @DisplayName("createPurchase with Not Purchasable Package Test")
    @Test
    void createPurchase_whenPackageIsNotPurchasable_thenReturn403andException() throws Exception {

        Customer dummyCusto = newCustomer("dummy");
        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(dummyCusto);
        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        AccountCreateRequestDto accountCreateRequestDto = new AccountCreateRequestDto(
                customerId,
                BigDecimal.valueOf(100),
                TariffType.PREMIUM
        );
        String accountId = accountService.createAccount(accountCreateRequestDto).getId();

        SubPackage dummyPackage = newForbiddenSubPackage(1L);
        PackageRequestDto packageRequestDto = newPackageRequestDto(dummyPackage);
        Long packageId = subPackageService.createPackage(packageRequestDto).getId();

        PurchaseCreateRequestDto purchaseCreateRequestDto = new PurchaseCreateRequestDto(
                accountId,
                packageId,
                (short) 25
        );

        this.mockMvc.perform(post("/v1/purchases/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(purchaseCreateRequestDto)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PurchaseNotPossibleException))
                .andExpect(result -> assertEquals(
                        "This package can not be purchased at this moment!",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @DisplayName("createPurchase with Blank Account Id Test")
    @Test
    void createPurchase_whenAccountIdIsBlank_thenReturn400andException() throws Exception {

        SubPackage dummyPackage = newSubPackage(1L);
        PackageRequestDto packageRequestDto = newPackageRequestDto(dummyPackage);
        Long packageId = subPackageService.createPackage(packageRequestDto).getId();

        PurchaseCreateRequestDto purchaseCreateRequestDto = new PurchaseCreateRequestDto(
                "",
                packageId,
                (short) 25
        );

        this.mockMvc.perform(post("/v1/purchases/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(purchaseCreateRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("createPurchase with Negative Package Price Test")
    @Test
    void createPurchase_whenPackagePriceIsNegative_thenReturn400andException() throws Exception {

        Customer dummyCusto = newCustomer("dummy");
        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(dummyCusto);
        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        AccountCreateRequestDto accountCreateRequestDto = new AccountCreateRequestDto(
                customerId,
                BigDecimal.valueOf(100),
                TariffType.PREMIUM
        );
        String accountId = accountService.createAccount(accountCreateRequestDto).getId();

        SubPackage dummyPackage = newSubPackage(1L);
        PackageRequestDto packageRequestDto = newPackageRequestDto(dummyPackage);
        Long packageId = subPackageService.createPackage(packageRequestDto).getId();

        PurchaseCreateRequestDto purchaseCreateRequestDto = new PurchaseCreateRequestDto(
                accountId,
                packageId,
                (short) -25
        );

        this.mockMvc.perform(post("/v1/purchases/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(purchaseCreateRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("createPurchase with Negative Package Id Test")
    @Test
    void createPurchase_whenPackageIdIsNegative_thenReturn400andException() throws Exception {

        Customer dummyCusto = newCustomer("dummy");
        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(dummyCusto);
        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        AccountCreateRequestDto accountCreateRequestDto = new AccountCreateRequestDto(
                customerId,
                BigDecimal.valueOf(100),
                TariffType.PREMIUM
        );
        String accountId = accountService.createAccount(accountCreateRequestDto).getId();

        SubPackage dummyPackage = newSubPackage(1L);
        PackageRequestDto packageRequestDto = newPackageRequestDto(dummyPackage);
        Long packageId = subPackageService.createPackage(packageRequestDto).getId();

        PurchaseCreateRequestDto purchaseCreateRequestDto = new PurchaseCreateRequestDto(
                accountId,
                -1,
                (short) -25
        );

        this.mockMvc.perform(post("/v1/purchases/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(purchaseCreateRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }*/

    @DisplayName("deletePurchaseById with Valid Id Test")
    @Test
    void deletePurchaseById_whenIdIsValid_thenDeletePurchase() throws Exception {

        Customer dummyCusto = newCustomer("dummy");
        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(dummyCusto);
        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        AccountCreateRequestDto accountCreateRequestDto = new AccountCreateRequestDto(
                customerId,
                BigDecimal.valueOf(100),
                TariffType.PREMIUM
        );
        String accountId = accountService.createAccount(accountCreateRequestDto).getId();

        SubPackage dummyPackage = newSubPackage(1L);
        PackageRequestDto packageRequestDto = newPackageRequestDto(dummyPackage);
        Long packageId = subPackageService.createPackage(packageRequestDto).getId();

        PurchaseCreateRequestDto purchaseCreateRequestDto = new PurchaseCreateRequestDto(
                accountId,
                packageId,
                (short) 25
        );

        PurchaseDto purchaseDto = purchaseService.createPurchase(purchaseCreateRequestDto);

        this.mockMvc.perform(delete("/v1/purchases/{purchaseId}", purchaseDto.getId()))
                .andExpect(status().isNoContent());
    }

    @DisplayName("deletePurchaseById with Invalid Id Test")
    @Test
    void deletePurchaseById_whenIdIsInValid_thenReturn404andException() throws Exception {

        this.mockMvc.perform(delete("/v1/purchases/{purchaseId}", "404"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))
                .andExpect(result -> assertEquals(
                        "No purchase found with this id: 404",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }
}