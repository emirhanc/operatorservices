package com.operatorservices.coreservice.service;

import com.operatorservices.coreservice.TestSupport;
import com.operatorservices.coreservice.dto.converter.ModelDtoConverter;
import com.operatorservices.coreservice.exception.EntryNotFoundException;
import com.operatorservices.coreservice.model.Account;
import com.operatorservices.coreservice.model.Customer;
import com.operatorservices.coreservice.repository.CustomerRepository;

import com.operatorservices.coreservice.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class CustomerServiceTest extends TestSupport {

    private CustomerRepository customerRepository;
    private ModelDtoConverter modelDtoConverter;
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        modelDtoConverter = mock(ModelDtoConverter.class);
        customerService = new CustomerService(customerRepository, modelDtoConverter);
    }

    @DisplayName("getCustomerById with Valid Id Test")
    @Test
    void whenGetCustomerByIdIsCalled_withValidId_itShouldReturnCustomerDto() {

       Customer customer = newCustomer("customerId");
       CustomerGetDto customerGetDto = newCustomerGetDto(customer);

       when(customerRepository.findById("customerId")).thenReturn(Optional.of(customer));
       when(modelDtoConverter.customerToCustomerGetDto(customer)).thenReturn(customerGetDto);

       CustomerGetDto test = customerService.getCustomerById("customerId");

       assertEquals(test, customerGetDto);

       verify(customerRepository).findById("customerId");
       verify(modelDtoConverter).customerToCustomerGetDto(customer);
    }

    @DisplayName("getCustomerById with Invalid Id Test")
    @Test
    void whenGetCustomerByIdIsCalled_withInvalidId_itShouldThrowEntryNotFoundException() {

        when(customerRepository.findById("invalidId")).thenReturn(Optional.empty());

        assertThrows(EntryNotFoundException.class,
                ()->customerService.getCustomerById("invalidId"));

        verify(customerRepository).findById("invalidId");
        verifyNoInteractions(modelDtoConverter);
    }

    @DisplayName("getAllCustomers Test")
    @Test
    void whenGetAllCustomersIsCalled_thenReturnAListOfCustomerGetDto() {

        Customer customer1 = newCustomer("customer-1");
        Customer customer2 = newCustomer("customer-2");

        when(customerRepository.findAll()).thenReturn(List.of(customer1, customer2));
        when(modelDtoConverter.customerToCustomerGetDto(customer1)).thenReturn(newCustomerGetDto(customer1));
        when(modelDtoConverter.customerToCustomerGetDto(customer2)).thenReturn(newCustomerGetDto(customer2));

        List<CustomerGetDto> test = customerService.getAllCustomers();

        assertEquals(test, List.of(newCustomerGetDto(customer1),newCustomerGetDto(customer2)));

        verify(customerRepository).findAll();
        verify(modelDtoConverter).customerToCustomerGetDto(customer1);
        verify(modelDtoConverter).customerToCustomerGetDto(customer2);
    }

    @DisplayName("getAllAccounts with Valid Id Test")
    @Test
    void whenGetAllAccountsIsCalled_withAValidId_itShouldReturnCustomerRequestGetAccountDto() {
        Customer customer = newCustomer("customerId");
        Account account1 = newAccount("account1", "customerId", 100L, Set.of());
        Account account2 = newAccount("account2", "customerId", 100L, Set.of());
        CustomerRequestGetAccountDto customerRequestGetAccountDto1 = newCustomerRequestGetAccountDto(account1);
        CustomerRequestGetAccountDto customerRequestGetAccountDto2 = newCustomerRequestGetAccountDto(account2);
        customer.getAccounts().addAll(List.of(account1, account2));

        when(customerRepository.findById("customerId")).thenReturn(Optional.of(customer));
        when(modelDtoConverter.accountToCustomerRequestGetAccountDto(account1))
                .thenReturn(customerRequestGetAccountDto1);
        when(modelDtoConverter.accountToCustomerRequestGetAccountDto(account2))
                .thenReturn(customerRequestGetAccountDto2);

        List<CustomerRequestGetAccountDto> test = customerService.getAllAccounts("customerId");

        assertEquals(
                test, List.of(
                        newCustomerRequestGetAccountDto(account1),
                        newCustomerRequestGetAccountDto(account2))
        );

        verify(customerRepository).findById("customerId");
        verify(modelDtoConverter).accountToCustomerRequestGetAccountDto(account1);
        verify(modelDtoConverter).accountToCustomerRequestGetAccountDto(account2);
    }

    @DisplayName("createCustomer Test")
    @Test
    void whenCreateCustomerIsCalled_withValidRequest_thenReturnCustomerDto() {

        Customer customer = newCustomer("customerId");
        CustomerCreateRequestDto customerCreateRequestDto = newCustomerCreateRequestDto(customer);
        CustomerDto customerDto = newCustomerDto(customer);

        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(modelDtoConverter.customerToCustomerDto(customer)).thenReturn(customerDto);

        CustomerDto test = customerService.createCustomer(customerCreateRequestDto);

        assertEquals(test, customerDto);

        verify(customerRepository).save(any(Customer.class));
        verify(modelDtoConverter).customerToCustomerDto(customer);
    }

    @DisplayName("updateCustomer Test with Valid Request")
    @Test
    void whenUpdateCustomerIsCalled_withValidRequest_thenReturnDto() {

        Customer customer = newCustomer("customerId");
        CustomerUpdateRequestDto customerUpdateRequestDto = newCustomerUpdateRequestDto(customer);
        CustomerDto customerDto = newCustomerDto(customer);

        when(customerRepository.findById("customerId")).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(modelDtoConverter.customerToCustomerDto(customer)).thenReturn(customerDto);

        CustomerDto test = customerService.updateCustomer(customerUpdateRequestDto, "customerId");

        assertEquals(test, customerDto);

        verify(customerRepository).findById("customerId");
        verify(customerRepository).save(customer);
        verify(modelDtoConverter).customerToCustomerDto(customer);
    }

    @DisplayName("deleteCustomer with Valid Id Test")
    @Test
    void whenDeleteCustomerCalled_withAValidId_itShouldDeleteCustomer() {
        when(customerRepository.existsById("id")).thenReturn(true);

        customerService.deleteCustomer("id");

        verify(customerRepository).existsById("id");
        verify(customerRepository).deleteById("id");
    }

    @DisplayName("deleteCustomer with Invalid Id Test")
    @Test
    void whenDeleteCustomerCalled_withAnInvalidId_itShouldThrowEntryNotFoundException() {
        when(customerRepository.existsById("invalidId")).thenReturn(false);

        assertThrows(EntryNotFoundException.class,
                ()->customerService.deleteCustomer("invalidId"));

        verify(customerRepository).existsById("invalidId");
        verify(customerRepository, times(0)).deleteById("invalidId");
    }
}