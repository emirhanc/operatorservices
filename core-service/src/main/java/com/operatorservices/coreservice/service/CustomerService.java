package com.operatorservices.coreservice.service;

import com.operatorservices.coreservice.dto.converter.ModelDtoConverter;
import com.operatorservices.coreservice.dto.*;
import com.operatorservices.coreservice.exception.EntryNotFoundException;
import com.operatorservices.coreservice.model.Customer;
import com.operatorservices.coreservice.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CustomerService {

   private final CustomerRepository customerRepository;
   private final ModelDtoConverter modelDtoConverter;

    public CustomerService(CustomerRepository customerRepository, ModelDtoConverter modelDtoConverter) {
        this.customerRepository = customerRepository;
        this.modelDtoConverter = modelDtoConverter;
    }

    protected Customer returnCustomerById(String id){
        return customerRepository.findById(id)
                .orElseThrow(
                        () -> new EntryNotFoundException("No customer found with this id: " + id));
    }

    public CustomerGetDto getCustomerById(String customerId){
        return modelDtoConverter.customerToCustomerGetDto(returnCustomerById(customerId));
    }

    public List<CustomerGetDto> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(modelDtoConverter::customerToCustomerGetDto)
                .collect(Collectors.toList());
    }

    public List<CustomerRequestGetAccountDto> getAllAccounts(String customerId) {
        return returnCustomerById(customerId).getAccounts()
                .stream()
                .map(modelDtoConverter::accountToCustomerRequestGetAccountDto)
                .collect(Collectors.toList());
    }

    public CustomerDto createCustomer(CustomerCreateRequestDto customerCreateRequestDto){

        Customer customer = new Customer(
                LocalDateTime.now(),
                customerCreateRequestDto.getName(),
                customerCreateRequestDto.getSurname(),
                customerCreateRequestDto.getEmail(),
                customerCreateRequestDto.getPassword()
        );
        return modelDtoConverter.customerToCustomerDto(customerRepository.save(customer));
    }

    public CustomerDto updateCustomer(CustomerUpdateRequestDto customerUpdateRequestDto, String customerId) {

        return customerRepository.findById(customerId)
                .map(customer -> {
                    customer.setEmail(Objects.requireNonNull(customerUpdateRequestDto.getEmail()));
                    customer.setPassword(Objects.requireNonNull(customerUpdateRequestDto.getPassword()));
                    return modelDtoConverter.customerToCustomerDto(customerRepository.save(customer));
                })
                .orElseThrow(
                        () -> new EntryNotFoundException("No customer found with this id: " + customerId));
    }

    public void deleteCustomer(String customerId){
        if (customerRepository.existsById(customerId)) {
            customerRepository.deleteById(customerId);
        } else {
            throw new EntryNotFoundException("No customer found with this id: " + customerId);
        }
    }
}

