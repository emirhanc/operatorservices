package com.operatorservices.purchaseorderservice.service;

import com.operatorservices.purchaseorderservice.dto.ExceptionDto;
import com.operatorservices.purchaseorderservice.dto.PurchaseOrderDto;
import com.operatorservices.purchaseorderservice.exception.EntryNotFoundException;
import com.operatorservices.purchaseorderservice.exception.InsufficientFundsException;
import com.operatorservices.purchaseorderservice.exception.PurchaseNotPossibleException;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ExecutionException;


@Service
public class PurchaseOrderService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderService.class);
    private static final String TOPIC = "purchase-order";
    private final ReplyingKafkaTemplate<String, PurchaseOrderDto, Object> replyingKafkaTemplate;

    public PurchaseOrderService(ReplyingKafkaTemplate<String, PurchaseOrderDto, Object> replyingKafkaTemplate) {
        this.replyingKafkaTemplate = replyingKafkaTemplate;
    }

    public Object sendPurchaseOrder (PurchaseOrderDto purchaseOrder) throws ExecutionException, InterruptedException {

        logger.info("New Purchase Order has been recorded {}", purchaseOrder);

        ProducerRecord<String, PurchaseOrderDto> record = new ProducerRecord<>(TOPIC, purchaseOrder);
        replyingKafkaTemplate.setSharedReplyTopic(true);
        RequestReplyFuture<String, PurchaseOrderDto, Object> future = replyingKafkaTemplate.sendAndReceive(record);

        Object response = Objects.requireNonNull(future.get()).value();

        if(response instanceof ExceptionDto){

            switch (((ExceptionDto) response).getCode()) {

                case 404:
                    throw new EntryNotFoundException(((ExceptionDto) response).getMessage());

                case 403:
                    throw new PurchaseNotPossibleException(((ExceptionDto) response).getMessage());

                case 402:
                    throw new InsufficientFundsException(((ExceptionDto) response).getMessage());

                default:
                    return "Undefined exception";
            }
        }
        return response;
    }

}
