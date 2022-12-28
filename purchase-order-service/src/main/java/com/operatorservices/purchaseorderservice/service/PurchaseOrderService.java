package com.operatorservices.purchaseorderservice.service;

import com.operatorservices.purchaseorderservice.dto.ErrorRecordDto;
import com.operatorservices.purchaseorderservice.dto.ExceptionDto;
import com.operatorservices.purchaseorderservice.dto.PurchaseOrderDto;
import com.operatorservices.purchaseorderservice.exception.EntryNotFoundException;
import com.operatorservices.purchaseorderservice.exception.InsufficientFundsException;
import com.operatorservices.purchaseorderservice.exception.PurchaseNotPossibleException;
import com.operatorservices.purchaseorderservice.model.ErrorRecord;
import com.operatorservices.purchaseorderservice.repository.ErrorRecordRepository;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


@Service
public class PurchaseOrderService {

    private final ErrorRecordRepository errorRecordRepository;
    private static final Logger logger = LoggerFactory.getLogger(PurchaseOrderService.class);
    private static final String TOPIC = "purchase-order";
    private final ReplyingKafkaTemplate<String, PurchaseOrderDto, Object> replyingKafkaTemplate;

    public PurchaseOrderService(ErrorRecordRepository errorRecordRepository,
                                ReplyingKafkaTemplate<String,
                                        PurchaseOrderDto,
                                        Object> replyingKafkaTemplate) {

        this.errorRecordRepository = errorRecordRepository;
        this.replyingKafkaTemplate = replyingKafkaTemplate;
    }

    protected void saveErrorRecord(ExceptionDto exceptionDto){
        ErrorRecord errorRecord = new ErrorRecord(exceptionDto.getCode(), exceptionDto.getMessage());
        errorRecordRepository.save(errorRecord);
    }

    public Object sendPurchaseOrder (PurchaseOrderDto purchaseOrder) throws ExecutionException, InterruptedException {

        logger.info("New Purchase Order has been recorded {}", purchaseOrder);

        ProducerRecord<String, PurchaseOrderDto> record = new ProducerRecord<>(TOPIC, purchaseOrder);
        replyingKafkaTemplate.setSharedReplyTopic(true);
        RequestReplyFuture<String, PurchaseOrderDto, Object> future = replyingKafkaTemplate.sendAndReceive(record);

        Object response = Objects.requireNonNull(future.get()).value();

        if(response instanceof ExceptionDto){

            saveErrorRecord((ExceptionDto) response);

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

    public List<ErrorRecordDto> getErrorRecords(){
        List<ErrorRecord> errorRecordList = (List<ErrorRecord>) errorRecordRepository.findAll();
        return errorRecordList.stream().map(ErrorRecordDto::convertToDto).collect(Collectors.toList());

    }

}
