package com.operatorservices.purchaseorderservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
public class InsufficientFundsException extends RuntimeException{

    public InsufficientFundsException(String errorMessage){ super (errorMessage); }
}
