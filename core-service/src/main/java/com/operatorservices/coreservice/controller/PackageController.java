package com.operatorservices.coreservice.controller;

import com.operatorservices.coreservice.controller.modelAssembler.PackageModelAssembler;
import com.operatorservices.coreservice.dto.PackageDto;
import com.operatorservices.coreservice.dto.PackageRequestDto;
import com.operatorservices.coreservice.dto.GetAccountsByPackageDto;
import com.operatorservices.coreservice.service.SubPackageService;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/packages")
public class PackageController {

    private final SubPackageService packageService;
    private final PackageModelAssembler modelAssembler;

    public PackageController(SubPackageService packageService, PackageModelAssembler modelAssembler) {
        this.packageService = packageService;
        this.modelAssembler = modelAssembler;
    }

    @GetMapping("/{packageId}")
    public ResponseEntity<EntityModel<PackageDto>> getPackageById(@PathVariable Long packageId){
        return ResponseEntity.ok(modelAssembler.toModel(packageService.getPackageById(packageId)));
    }

    @GetMapping("/{packageId}/accounts")
    public ResponseEntity<CollectionModel<GetAccountsByPackageDto>>getAccountsByPackage(@PathVariable Long packageId){

        CollectionModel<GetAccountsByPackageDto> body = CollectionModel.of(
                packageService.getAccountsByPackage(packageId)
                        .stream()
                        .map(getAccountsByPackageDto -> {
                                getAccountsByPackageDto.add(linkTo(methodOn(AccountController.class)
                                        .getAccountById(getAccountsByPackageDto.getId())).withSelfRel());
                                getAccountsByPackageDto.add(linkTo(methodOn(AccountController.class)
                                        .getAllAccounts()).withRel(IanaLinkRelations.COLLECTION));
                                getAccountsByPackageDto.add(linkTo(methodOn(AccountController.class)
                                        .getPurchasesByAccountId(getAccountsByPackageDto.getId())).withRel("purchases"));
                                return getAccountsByPackageDto;
                        }).collect(Collectors.toList()));

        body.add(linkTo(methodOn(PackageController.class).getAccountsByPackage(packageId)).withSelfRel());
        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PackageDto>>> getAllPackages() {
        return ResponseEntity.ok(
                modelAssembler.toCollectionModel(packageService.getAllPackages())
                        .add(linkTo(methodOn(PackageController.class).getAllPackages()).withSelfRel()));
    }

    @PostMapping
    public ResponseEntity<EntityModel<PackageDto>> createPackage(
            @Valid @RequestBody PackageRequestDto packageRequestDto){
        return new ResponseEntity<>(
                modelAssembler.toModel(packageService.createPackage(packageRequestDto)),
                HttpStatus.CREATED);
    }

    @PutMapping("/{packageId}")
    public ResponseEntity<EntityModel<PackageDto>> updatePackage(
            @Valid @RequestBody PackageRequestDto packageRequestDto,
            @PathVariable Long packageId){
        return ResponseEntity.ok(
                modelAssembler.toModel(packageService.updatePackage(packageRequestDto, packageId)));
    }
}


