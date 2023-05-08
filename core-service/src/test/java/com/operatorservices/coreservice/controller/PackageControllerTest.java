package com.operatorservices.coreservice.controller;

import com.operatorservices.coreservice.IntegrationSetup;
import com.operatorservices.coreservice.dto.AccountCreateRequestDto;
import com.operatorservices.coreservice.dto.CustomerCreateRequestDto;
import com.operatorservices.coreservice.dto.PackageRequestDto;
import com.operatorservices.coreservice.dto.PurchaseCreateRequestDto;
import com.operatorservices.coreservice.exception.EntryNotFoundException;
import com.operatorservices.coreservice.model.Customer;
import com.operatorservices.coreservice.model.PackageType;
import com.operatorservices.coreservice.model.SubPackage;
import com.operatorservices.coreservice.model.TariffType;
import com.operatorservices.coreservice.repository.SubPackageRepository;
import com.operatorservices.coreservice.service.AccountService;
import com.operatorservices.coreservice.service.CustomerService;
import com.operatorservices.coreservice.service.PurchaseService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.util.Objects;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class PackageControllerTest extends IntegrationSetup {

    @Autowired
    private SubPackageRepository packageRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private PurchaseService purchaseService;

    @DisplayName("getPackageById with Valid Id Test")
    @Test
    void getPackageById_whenIdIsValid_thenReturnPackage() throws Exception {

        SubPackage subPackage = newSubPackage(1L);
        Long packageId = packageRepository.save(subPackage).getId();

        this.mockMvc.perform(get("/v1/packages/{packageId}", packageId))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id", is(Objects.requireNonNull(packageId).intValue())))
                .andExpect(jsonPath("$.name", is(subPackage.getName())))
                .andExpect(jsonPath("$.packageType", is(String.valueOf(subPackage.getPackageType()))))
                .andExpect(jsonPath("$.duration", is((int)subPackage.getDuration())))
                .andExpect(jsonPath("$.purchasable", is(subPackage.getPurchasable())))

                .andExpect(jsonPath("_links.self.href",
                        is("http://localhost/v1/packages/" + packageId)))

                .andExpect(jsonPath("_links.collection.href",
                        is("http://localhost/v1/packages")))

                .andExpect(jsonPath("_links.accounts.href",
                        is("http://localhost/v1/packages/" + packageId + "/accounts")));
    }

    @DisplayName("getPackageById with Invalid Id Test")
    @Test
    void getPackageById_whenIdIsInvalid_thenReturn404andException() throws Exception {

        this.mockMvc.perform(get("/v1/packages/{packageId}", 404))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))
                .andExpect(result -> assertEquals(
                        "No package found with this id: 404",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @DisplayName("getAccountsByPackage with Valid Id Test")
    @Test
    void getAccountsByPackage_whenIdIsValid_thenReturnAListOfAccounts() throws Exception {

        SubPackage subPackage = newSubPackage(1L);
        Long packageId = packageRepository.save(subPackage).getId();

        Customer dummyCusto = newCustomer("dummyCusto");
        CustomerCreateRequestDto customerCreateRequestDto =
                newCustomerCreateRequestDto(Objects.requireNonNull(dummyCusto));

        String customerId = customerService.createCustomer(customerCreateRequestDto).getId();

        AccountCreateRequestDto accountCreateRequestDto1 = new AccountCreateRequestDto(
                customerId,
                BigDecimal.valueOf(100L),
                TariffType.STANDARD
        );

        AccountCreateRequestDto accountCreateRequestDto2 = new AccountCreateRequestDto(
                customerId,
                BigDecimal.valueOf(100L),
                TariffType.STANDARD
        );

        String account1Id = accountService.createAccount(accountCreateRequestDto1).getId();
        String account2Id = accountService.createAccount(accountCreateRequestDto2).getId();

        PurchaseCreateRequestDto purchaseCreateRequestDto1 = new PurchaseCreateRequestDto(
                account1Id,
                packageId,
                (short) 100
        );

        PurchaseCreateRequestDto purchaseCreateRequestDto2 = new PurchaseCreateRequestDto(
                account2Id,
                packageId,
                (short) 100
        );

        purchaseService.createPurchase(purchaseCreateRequestDto1);
        purchaseService.createPurchase(purchaseCreateRequestDto2);

        this.mockMvc.perform(get("/v1/packages/{packageId}/accounts", packageId))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.size()", is(2)));
    }

    @DisplayName("getAccountsByPackage with Invalid Id Test")
    @Test
    void getAccountsByPackage_whenIdIsInvalid_thenReturnAListOfAccounts() throws Exception {

        this.mockMvc.perform(get("/v1/packages/{packageId}/accounts", 404))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))
                .andExpect(result -> assertEquals(
                        "No package found with this id: 404",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @DisplayName("getAllPackages when Package Exists Test")
    @Test
    void getAllPackages_whenPackageExists_thenReturnPackage() throws Exception {

        SubPackage subPackage1 = newSubPackage(1L);
        SubPackage subPackage2 = newSubPackage(2L);
        packageRepository.save(subPackage1);
        packageRepository.save(subPackage2);

        this.mockMvc.perform(get("/v1/packages"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.size()", is(2)));
    }

    @DisplayName("createPackage with valid Request Test")
    @Test
    void createPackage_whenRequestIsValid_thenReturnPackage() throws Exception {

        SubPackage subPackage = newSubPackage(1L);
        PackageRequestDto packageRequestDto = newPackageRequestDto(subPackage);

        this.mockMvc.perform(post("/v1/packages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(packageRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.name", is(subPackage.getName())))
                .andExpect(jsonPath("$.packageType", is(String.valueOf(subPackage.getPackageType()))))
                .andExpect(jsonPath("$.duration", is((int)subPackage.getDuration())))
                .andExpect(jsonPath("$.purchasable", is(subPackage.getPurchasable())))

                .andExpect(jsonPath("_links.self.href", notNullValue()))

                .andExpect(jsonPath("_links.collection.href",
                        is("http://localhost/v1/packages")))

                .andExpect(jsonPath("_links.accounts.href", notNullValue()));
    }

    @DisplayName("createPackage with Blank Name Test")
    @Test
    void createPackage_whenNameIsBlank_thenReturn400andException() throws Exception {

        PackageRequestDto packageRequestDto = new PackageRequestDto(
                "",
                PackageType.COMBO,
                true,
                6
        );

        this.mockMvc.perform(post("/v1/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(packageRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("createPackage with Negative Duration Test")
    @Test
    void createPackage_whenDurationIsNegative_thenReturn400andException() throws Exception {

        PackageRequestDto packageRequestDto = new PackageRequestDto(
                "Name",
                PackageType.COMBO,
                true,
                -6
        );

        this.mockMvc.perform(post("/v1/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(packageRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("updatePackage with Valid Request Test")
    @Test
    void updatePackage_whenRequestIsValid_thenUpdatePackage() throws Exception {

        SubPackage subPackage = newSubPackage(1L);
        Long packageId = packageRepository.save(subPackage).getId();
        PackageRequestDto packageRequestDto = newPackageRequestDto(subPackage);

        this.mockMvc.perform(put("/v1/packages/{packageId}", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(packageRequestDto)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id", is(packageId.intValue())))
                .andExpect(jsonPath("$.name", is(packageRequestDto.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.packageType", Matchers.is(String.valueOf(packageRequestDto.getPackageType()))))
                .andExpect(jsonPath("$.duration", is((int)packageRequestDto.getDuration())))
                .andExpect(jsonPath("$.purchasable", is(packageRequestDto.getPurchasable())))

                .andExpect(jsonPath("_links.self.href",
                        is("http://localhost/v1/packages/" + packageId)))

                .andExpect(jsonPath("_links.collection.href",
                        is("http://localhost/v1/packages")))

                .andExpect(jsonPath("_links.accounts.href",
                        is("http://localhost/v1/packages/" + packageId + "/accounts")));
    }

    @DisplayName("updatePackage with Blank Name Test")
    @Test
    void updatePackage_whenNameIsBlank_thenReturn400andException() throws Exception {

        SubPackage subPackage = newSubPackage(1L);
        Long packageId = packageRepository.save(subPackage).getId();

        PackageRequestDto packageRequestDto = new PackageRequestDto(
                "",
                PackageType.COMBO,
                true,
                6
        );

        this.mockMvc.perform(put("/v1/packages/{packageId}", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(packageRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("updatePackage with Negative Duration Test")
    @Test
    void updatePackage_whenDurationIsNegative_thenReturn400andException() throws Exception {

        SubPackage subPackage = newSubPackage(1L);
        Long packageId = packageRepository.save(subPackage).getId();

        PackageRequestDto packageRequestDto = new PackageRequestDto(
                "Name",
                PackageType.COMBO,
                true,
                -6
        );

        this.mockMvc.perform(put("/v1/packages/{packageId}", packageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(packageRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    /*
    @DisplayName("deletePackageById with Valid Id Test")
    @Test
    void deletePackageById_whenIdIsValid_thenDeletePackage() throws Exception {

        SubPackage subPackage = newSubPackage(1L);
        Long packageId = packageRepository.save(subPackage).getId();

        this.mockMvc.perform(delete("/v1/packages/{packageId}/", packageId))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @DisplayName("deletePackageById with Invalid Id Test")
    @Test
    void deletePackageById_whenIdIsInvalid_thenReturn404andException() throws Exception {

        this.mockMvc.perform(delete("/v1/packages/{packageId}/", 404))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))
                .andExpect(result -> assertEquals(
                        "No package found with this id: 404",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }
     */
}