package com.operatorservices.notificationservice.service;

import com.operatorservices.notificationservice.dto.PurchaseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @KafkaListener(topics = "notification", groupId = "notification")
    public void consume(PurchaseDto purchaseDto){
        logger.info("Purchase Confirmed {}", purchaseDto);
    }

}
