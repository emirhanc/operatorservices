package com.operatorservices.coreservice.controller;

import com.operatorservices.coreservice.dto.CustomerCreateRequestDto;
import com.operatorservices.coreservice.dto.CustomerUpdateRequestDto;
import com.operatorservices.coreservice.exception.EntryNotFoundException;
import com.operatorservices.coreservice.model.Customer;
import com.operatorservices.coreservice.IntegrationSetup;
import com.operatorservices.coreservice.repository.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CustomerControllerTest extends IntegrationSetup {

    @Autowired
    private CustomerRepository customerRepository;

    @DisplayName("getCustomerById when Id Is Valid Test")
    @Test
    void getCustomerById_whenIdIsValid_thenReturnCustomer() throws Exception {

        Customer customer = newCustomer("-");
        String customerId = customerRepository.save(customer).getId();

        this.mockMvc.perform(get("/v1/customers/{customerId}", customerId))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id", is(customerId)))
                .andExpect(jsonPath("$.creationDate", notNullValue()))
                .andExpect(jsonPath("$.name", is(customer.getName())))
                .andExpect(jsonPath("$.surname", is(customer.getSurname())))
                .andExpect(jsonPath("$.email", is(customer.getEmail())))
                .andExpect(jsonPath("$.accounts", hasSize(0)))

                .andExpect(jsonPath("_links.self.href",
                        is("http://localhost/v1/customers/" + customerId)))

                .andExpect(jsonPath("_links.collection.href",
                        is("http://localhost/v1/customers")))

                .andExpect(jsonPath("_links.accounts.href",
                        is("http://localhost/v1/customers/" + customerId + "/accounts")));
    }

    @DisplayName("getCustomerById when Id Is Invalid Test")
    @Test
    void getCustomerById_whenIdIsInvalid_thenReturn404AndException() throws Exception {

        this.mockMvc.perform(get("/v1/customers/{customerId}", "404"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))
                .andExpect(result -> assertEquals(
                                "No customer found with this id: 404",
                                Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @DisplayName("getAllCustomers when Customer Exists Test")
    @Test
    void getAllCustomers_whenCustomerExists_thenReturnListOfCustomers() throws Exception {

        customerRepository.save(newCustomer("1")).getId();
        customerRepository.save(newCustomer("2")).getId();

        this.mockMvc.perform(get("/v1/customers"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.size()", is(2)));
    }

    @DisplayName("getAllAccounts when customerId Valid Test")
    @Test
    void getAllAccounts_whenCustomerIdExists_thenReturnListOfAccounts() throws Exception {

        Customer dummy = newCustomer("dummy");
        Customer customer = customerRepository.save(dummy);

        customer.getAccounts().add(newAccount("account1", customer.getId(), 100L, Set.of()));
        customer.getAccounts().add(newAccount("account2", customer.getId(), 100L, Set.of()));

        customerRepository.save(customer);

        this.mockMvc.perform(get("/v1/customers/{customerId}/accounts", customer.getId()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.size()", is(2)));
    }

    @DisplayName("getAllAccounts when customerId Invalid Test")
    @Test
    void getAllAccounts_whenCustomerIdIsInvalid_thenReturn404andException() throws Exception {

        this.mockMvc.perform(get("/v1/customers/{customerId}/accounts", "404"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))
                .andExpect(result -> assertEquals(
                        "No customer found with this id: 404",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @DisplayName("createCustomer when Request Is Valid Test")
    @Test
    void createCustomer_whenRequestIsValid_thenCreateAndReturnCustomer() throws Exception {

        Customer customer = newCustomer("customerId");
        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(customer);

        this.mockMvc.perform(post("/v1/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(customerCreateRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.creationDate", notNullValue()))
                .andExpect(jsonPath("$.name", is(customer.getName())))
                .andExpect(jsonPath("$.surname", is(customer.getSurname())))
                .andExpect(jsonPath("$.email", is(customer.getEmail())))
                .andExpect(jsonPath("$.password", is(customer.getPassword())))
                .andExpect(jsonPath("$.accounts", hasSize(0)))

                .andExpect(jsonPath("_links.self.href", notNullValue()))

                .andExpect(jsonPath("_links.collection.href",
                        is("http://localhost/v1/customers")))

                .andExpect(jsonPath("_links.accounts.href", notNullValue()));
    }

    @DisplayName("createCustomer Request with Blank Name Test")
    @Test
    void createCustomer_whenNameIsBlank_thenReturn400AndException() throws Exception {

        Customer customer = new Customer(
                LocalDateTime.now(),
                "",
                "surname",
                "email@com",
                "password"
        );

        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(customer);

        this.mockMvc.perform(post("/v1/customers/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writer().writeValueAsString(customerCreateRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("createCustomer Request with Blank Surname Test")
    @Test
    void createCustomer_whenSurnameIsBlank_thenReturn400AndException() throws Exception {

        Customer customer = new Customer(
                LocalDateTime.now(),
                "name",
                "",
                "email@com",
                "password"
        );

        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(customer);

        this.mockMvc.perform(post("/v1/customers/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(customerCreateRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("createCustomer Request with Invalid Password Test")
    @Test
    void createCustomer_whenPasswordIsInvalid_thenReturn400AndException() throws Exception {

        Customer customer = new Customer(
                LocalDateTime.now(),
                "name",
                "surname",
                "email@com",
                "pass"              //invalid pass(length<8)
        );

        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(customer);

        this.mockMvc.perform(post("/v1/customers/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(customerCreateRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("createCustomer Request with Blank Password Test")
    @Test
    void createCustomer_whenPasswordIsBlank_thenReturn400AndException() throws Exception {

        Customer customer = new Customer(
                LocalDateTime.now(),
                "name",
                "surname",
                "email@com",
                ""
        );

        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(customer);

        this.mockMvc.perform(post("/v1/customers/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(customerCreateRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }


    @DisplayName("createCustomer Request with Invalid Email Test")
    @Test
    void createCustomer_whenEmailIsInvalid_thenReturn400AndException() throws Exception {

        Customer customer = new Customer(
                LocalDateTime.now(),
                "name",
                "surname",
                "InvalidEmail",
                "password"
        );

        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(customer);

        this.mockMvc.perform(post("/v1/customers/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(customerCreateRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));

                /*
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.status", is(String.valueOf(HttpStatus.BAD_REQUEST))))
                .andExpect(jsonPath("$['errors(1)'][0]",
                        is("email must be a well-formed email address"))); */
    }

    @DisplayName("createCustomer Request with Blank Email Test")
    @Test
    void createCustomer_whenEmailIsBlank_thenReturn400AndException() throws Exception {

        Customer customer = new Customer(
                LocalDateTime.now(),
                "name",
                "surname",
                "",
                "password"
        );

        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(customer);

        this.mockMvc.perform(post("/v1/customers/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(customerCreateRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("updateCustomer with Valid Request Test")
    @Test
    void updateCustomer_whenRequestIsValid_thenUpdateCustomer() throws Exception {

        Customer dummy = newCustomer("dummy");
        Customer customer = customerRepository.save(dummy);

        CustomerUpdateRequestDto customerUpdateRequestDto = new CustomerUpdateRequestDto(
                "newmmail@com",
                "newpassword"
        );

        this.mockMvc.perform(patch("/v1/customers/{customerId}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(customerUpdateRequestDto)))
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.id", is(customer.getId())))
                .andExpect(jsonPath("$.creationDate", notNullValue()))
                .andExpect(jsonPath("$.name", is(customer.getName())))
                .andExpect(jsonPath("$.surname", is(customer.getSurname())))
                .andExpect(jsonPath("$.email", is(customerUpdateRequestDto.getEmail())))
                .andExpect(jsonPath("$.password", is(customerUpdateRequestDto.getPassword())))
                .andExpect(jsonPath("$.accounts", hasSize(0)))

                .andExpect(jsonPath("_links.self.href",
                        is("http://localhost/v1/customers/" + customer.getId())))

                .andExpect(jsonPath("_links.collection.href",
                        is("http://localhost/v1/customers")))

                .andExpect(jsonPath("_links.accounts.href",
                        is("http://localhost/v1/customers/" + customer.getId() + "/accounts")));
    }

    @DisplayName("updateCustomer with Invalid Id Test")
    @Test
    void updateCustomer_whenIdIsInvalid_thenReturn404AndException() throws Exception {

        CustomerUpdateRequestDto body = newCustomerUpdateRequestDto(newCustomer("dummy"));

        this.mockMvc.perform(patch("/v1/customers/{customerId}", "404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))

                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))

                .andExpect(result -> assertEquals(
                        "No customer found with this id: 404",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @DisplayName("updateCustomer with Invalid Email Test")
    @Test
    void updateCustomer_whenEmailIsInvalid_thenReturn404AndException() throws Exception {

        Customer dummy = newCustomer("dummy");
        Customer customer = customerRepository.save(dummy);


        CustomerUpdateRequestDto customerUpdateRequestDto = new CustomerUpdateRequestDto(
                "invalidEmail",
                "newpasword"
        );

        this.mockMvc.perform(patch("/v1/customers/{customerId}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(customerUpdateRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("updateCustomer with Blank Email Test")
    @Test
    void updateCustomer_whenEmailIsBlank_thenReturn400AndException() throws Exception {

        Customer dummy = newCustomer("dummy");
        Customer customer = customerRepository.save(dummy);

        CustomerUpdateRequestDto customerUpdateRequestDto = new CustomerUpdateRequestDto(
                "",
                "newpasword"
        );

        this.mockMvc.perform(patch("/v1/customers/{customerId}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(customerUpdateRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }
    @DisplayName("updateCustomer with Invalid Password Test")
    @Test
    void updateCustomer_whenPasswordIsInvalid_thenReturn400AndException() throws Exception {

        Customer dummy = newCustomer("dummy");
        Customer customer = customerRepository.save(dummy);

        CustomerUpdateRequestDto customerUpdateRequestDto = new CustomerUpdateRequestDto(
                "newemail.com",
                "tooloongpassword"
        );

        this.mockMvc.perform(patch("/v1/customers/{customerId}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(customerUpdateRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("updateCustomer with Blank Password Test")
    @Test
    void updateCustomer_whenPasswordIsBlank_thenReturn400AndException() throws Exception {

        Customer dummy = newCustomer("dummy");
        Customer customer = customerRepository.save(dummy);

        CustomerUpdateRequestDto customerUpdateRequestDto = new CustomerUpdateRequestDto(
                "newemail.com",
                ""
        );

        this.mockMvc.perform(patch("/v1/customers/{customerId}", customer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writer().writeValueAsString(customerUpdateRequestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MethodArgumentNotValidException));
    }

    @DisplayName("deleteCustomerId with Valid Id Test")
    @Test
    void deleteCustomerById_whenIdIsValid_thenDeleteCustomer() throws Exception {

        Customer dummy = newCustomer("dummy");
        Customer customer = customerRepository.save(dummy);

        this.mockMvc.perform(delete("/v1/customers/{customerId}", customer.getId()))
                .andExpect(status().isNoContent());
    }

    @DisplayName("deleteCustomerId with Invalid Id Test")
    @Test
    void deleteCustomerById_whenIdIsInvalid_thenReturn404AndException() throws Exception {

        this.mockMvc.perform(get("/v1/customers/{customerId}", "404"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof EntryNotFoundException))
                .andExpect(result -> assertEquals(
                        "No customer found with this id: 404",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

}