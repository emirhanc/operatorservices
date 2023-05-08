package com.operatorservices.coreservice.controller;

import com.operatorservices.coreservice.controller.modelAssembler.AccountModelAssembler;
import com.operatorservices.coreservice.service.AccountService;
import com.operatorservices.coreservice.dto.AccountCreateRequestDto;
import com.operatorservices.coreservice.dto.AccountDto;
import com.operatorservices.coreservice.dto.AccountUpdateRequestDto;
import com.operatorservices.coreservice.dto.PurchaseDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

    public AccountController(AccountService accountService, AccountModelAssembler modelAssembler) {
        this.accountService = accountService;
        this.modelAssembler = modelAssembler;
    }

    private final AccountService accountService;
    private final AccountModelAssembler modelAssembler;

    @GetMapping("/{accountId}")
    public ResponseEntity<EntityModel<AccountDto>> getAccountById(@PathVariable String accountId){
        return ResponseEntity.ok(modelAssembler.toModel(accountService.getAccountById(accountId)));
    }

    @GetMapping("/{accountId}/purchases")
    public ResponseEntity<CollectionModel<PurchaseDto>> getPurchasesByAccountId(@PathVariable String accountId) {

        CollectionModel<PurchaseDto> body = CollectionModel.of(
                accountService.getAllPurchases(accountId)
                        .stream()
                        .map(purchaseDto ->
                                purchaseDto.add(linkTo(methodOn(PurchaseController.class)
                                        .getPurchaseById(purchaseDto.getId())).withSelfRel()))
                        .collect(Collectors.toList()));
        body.add(linkTo(methodOn(AccountController.class).getPurchasesByAccountId(accountId)).withSelfRel());
        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<AccountDto>>> getAllAccounts() {
        return ResponseEntity.ok(
                modelAssembler.toCollectionModel(accountService.getAllAccounts())
                        .add(linkTo(methodOn(AccountController.class).getAllAccounts()).withSelfRel()));
    }

    @PostMapping
    public ResponseEntity<EntityModel<AccountDto>> createAccount(@Valid @RequestBody AccountCreateRequestDto accountCreateRequestDto){

        AccountDto body = accountService.createAccount(accountCreateRequestDto);
        return new ResponseEntity<>(modelAssembler.toModel(body), HttpStatus.CREATED);
    }

    @PatchMapping("/{accountId}")
    public ResponseEntity<EntityModel<AccountDto>> updateAccount(@Valid @RequestBody AccountUpdateRequestDto accountUpdateDto,
                                                    @PathVariable String accountId){

        AccountDto body = accountService.updateAccount(accountUpdateDto, accountId);
        return ResponseEntity.ok(modelAssembler.toModel(body));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<?> deleteAccountById(@PathVariable String accountId){
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }
}