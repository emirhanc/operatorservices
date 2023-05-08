package com.operatorservices.coreservice.controller;

import com.operatorservices.coreservice.service.CustomerService;
import com.operatorservices.coreservice.dto.*;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/customers")
public class CustomerController {

    private final CustomerService customerService;


    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;

    }

    @GetMapping(value = "/{customerId}")
    public ResponseEntity<CustomerGetDto> getCustomerById(@PathVariable String customerId){

        CustomerGetDto body = customerService.getCustomerById(customerId);
        body.add(linkTo(methodOn(CustomerController.class).getCustomerById(customerId)).withSelfRel());
        body.add(linkTo(methodOn(CustomerController.class).getAllCustomers()).withRel(IanaLinkRelations.COLLECTION));
        body.add(linkTo(methodOn(CustomerController.class).getAllAccounts(customerId)).withRel("accounts"));
        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<CustomerGetDto>> getAllCustomers() {

        CollectionModel<CustomerGetDto> body = CollectionModel.of(customerService.getAllCustomers()
                .stream()
                .map(customerGetDto -> {
                        customerGetDto.add(linkTo(methodOn(CustomerController.class)
                                .getCustomerById(customerGetDto.getId())).withSelfRel());
                        customerGetDto.add(linkTo(methodOn(CustomerController.class)
                                .getAllAccounts(customerGetDto.getId())).withRel("accounts"));
                        return customerGetDto;
                        }
                ).collect(Collectors.toList()));

       body.add(linkTo(methodOn(CustomerController.class).getAllCustomers()).withSelfRel());

        return ResponseEntity.ok(body);
    }

    @GetMapping("/{customerId}/accounts")
    public ResponseEntity<CollectionModel<CustomerRequestGetAccountDto>> getAllAccounts(@PathVariable String customerId){

        CollectionModel<CustomerRequestGetAccountDto> body = CollectionModel.of(
                customerService.getAllAccounts(customerId)
                        .stream()
                        .map(customerRequestGetAccountDto ->
                                customerRequestGetAccountDto.add(linkTo(methodOn(AccountController.class)
                                        .getAccountById(customerRequestGetAccountDto.getId())).withSelfRel()))
                        .collect(Collectors.toList()));

        body.add(linkTo(methodOn(CustomerController.class).getAllAccounts(customerId)).withSelfRel());

        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(
            @Valid @RequestBody CustomerCreateRequestDto customerCreateRequestDto){

        CustomerDto body = customerService.createCustomer(customerCreateRequestDto);
        body.add(linkTo(methodOn(CustomerController.class).getCustomerById(body.getId())).withSelfRel());
        body.add(linkTo(methodOn(CustomerController.class).getAllCustomers()).withRel(IanaLinkRelations.COLLECTION));
        body.add(linkTo(methodOn(CustomerController.class).getAllAccounts(body.getId())).withRel("accounts"));

        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PatchMapping("/{customerId}")
    public ResponseEntity<CustomerDto> updateCustomer(
            @Valid @RequestBody CustomerUpdateRequestDto customerUpdateRequestDto,
            @PathVariable String customerId){

        CustomerDto body = customerService.updateCustomer(customerUpdateRequestDto, customerId);
        body.add(linkTo(methodOn(CustomerController.class).getCustomerById(body.getId())).withSelfRel());
        body.add(linkTo(methodOn(CustomerController.class).getAllCustomers()).withRel(IanaLinkRelations.COLLECTION));
        body.add(linkTo(methodOn(CustomerController.class).getAllAccounts(customerId)).withRel("accounts"));

        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<?> deleteCustomerById(@PathVariable String customerId){
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }
}