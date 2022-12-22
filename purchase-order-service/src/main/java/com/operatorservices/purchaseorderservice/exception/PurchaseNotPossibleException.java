package com.operatorservices.purchaseorderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class PurchaseNotPossibleException extends RuntimeException{

    public PurchaseNotPossibleException(String errorMessage){
        super(errorMessage);
    }
}