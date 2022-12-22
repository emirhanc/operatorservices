package com.operatorservices.coreservice.exception;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.*;


@ControllerAdvice
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    @NotNull
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NotNull MethodArgumentNotValidException exception,
            @NotNull HttpHeaders headers,
            @NotNull HttpStatus status,
            @NotNull WebRequest request
    ){
        Map<String, Object> errorBody = new LinkedHashMap<>();
        errorBody.put("timestamp", String.valueOf(LocalDateTime.now().withNano(0)));
        errorBody.put("status", String.valueOf(status));

        ArrayList<String> errors = new ArrayList<>();

        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.add(fieldError.getField() + " " + fieldError.getDefaultMessage()));

        exception.getBindingResult().getGlobalErrors().forEach(objectError ->
                errors.add(objectError.getObjectName() + " " + objectError.getDefaultMessage()));

        errorBody.put("errors" + "(" +errors.size() +")", errors);

        return new ResponseEntity<>(errorBody, headers, status);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(EntryNotFoundException.class)
    String entryNotFoundHandler(EntryNotFoundException exception) {
        return exception.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    @ExceptionHandler(InsufficientFundsException.class)
    String insufficientFundsHandler(InsufficientFundsException exception){
        return exception.getMessage();
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(PurchaseNotPossibleException.class)
    String purchaseNotPossibleHandler(PurchaseNotPossibleException exception) { return exception.getMessage(); }

}
