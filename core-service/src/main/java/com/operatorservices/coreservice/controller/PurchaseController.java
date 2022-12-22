package com.operatorservices.coreservice.controller;

import com.operatorservices.coreservice.dto.PurchaseDto;
import com.operatorservices.coreservice.service.PurchaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/v1/purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping("/{purchaseId}")
    public ResponseEntity<PurchaseDto> getPurchaseById(@PathVariable String purchaseId){
        return ResponseEntity.ok(
                purchaseService.getPurchaseById(purchaseId)
                        .add(linkTo(methodOn(PurchaseController.class).getPurchaseById(purchaseId)).withSelfRel()));
    }

    @DeleteMapping("/{purchaseId}")
    public ResponseEntity<?> deletePurchaseById(@PathVariable String purchaseId){
        purchaseService.deletePurchase(purchaseId);
        return ResponseEntity.noContent().build();
    }
}
