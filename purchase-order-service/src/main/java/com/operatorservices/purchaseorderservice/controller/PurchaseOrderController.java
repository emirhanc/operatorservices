package com.operatorservices.purchaseorderservice.controller;

import com.operatorservices.purchaseorderservice.dto.ErrorRecordDto;
import com.operatorservices.purchaseorderservice.dto.PurchaseOrderDto;
import com.operatorservices.purchaseorderservice.service.PurchaseOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/v1/purchase-order")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    public ResponseEntity<Object> sendPurchaseOrder(@Valid @RequestBody PurchaseOrderDto purchaseOrderDto)
            throws ExecutionException, InterruptedException {

        Object reply = purchaseOrderService.sendPurchaseOrder(purchaseOrderDto);
        return new ResponseEntity<>(reply, HttpStatus.CREATED);
    }

    @GetMapping("/errors")
    public ResponseEntity<List<ErrorRecordDto>> getAllErrors(){
        List<ErrorRecordDto> errorRecordDtoList = purchaseOrderService.getErrorRecords();
        return ResponseEntity.ok(errorRecordDtoList);
    }

}
