package com.operatorservices.coreservice.service;

import com.operatorservices.coreservice.dto.ExceptionDto;
import com.operatorservices.coreservice.exception.EntryNotFoundException;
import com.operatorservices.coreservice.exception.InsufficientFundsException;
import com.operatorservices.coreservice.exception.PurchaseNotPossibleException;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service(value = "orderErrorHandler")
public class PurchaseOrderErrorHandler implements ConsumerAwareListenerErrorHandler {

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer) {

        if (exception.getCause() instanceof EntryNotFoundException){

            return new ExceptionDto((short) 404, exception.getCause().getMessage());
        }

        if (exception.getCause() instanceof InsufficientFundsException){

            return new ExceptionDto((short) 402, exception.getCause().getMessage());
        }

        if (exception.getCause() instanceof PurchaseNotPossibleException){

            return new ExceptionDto((short) 403, exception.getCause().getMessage());
        }

        return null;
    }
}
