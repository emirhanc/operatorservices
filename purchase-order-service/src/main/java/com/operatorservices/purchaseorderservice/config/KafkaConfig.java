package com.operatorservices.purchaseorderservice.config;

import com.operatorservices.purchaseorderservice.dto.PurchaseOrderDto;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

import java.util.Properties;
import java.util.UUID;


@Configuration
public class KafkaConfig {

    @Value("${kafka.reply-topic}")
    private String replyTopic;

    @Bean
    public ReplyingKafkaTemplate<String, PurchaseOrderDto, Object> replyingKafkaTemplate(
      ProducerFactory<String, PurchaseOrderDto> producerFactory,
          ConcurrentKafkaListenerContainerFactory<String, Object> listenerContainerFactory
    ){
        ConcurrentMessageListenerContainer<String, Object> replyContainer
                = listenerContainerFactory.createContainer(replyTopic);

        replyContainer.getContainerProperties().setGroupId(UUID.randomUUID().toString());
        replyContainer.getContainerProperties().setMissingTopicsFatal(false);

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        replyContainer.getContainerProperties().setKafkaConsumerProperties(properties);

        return new ReplyingKafkaTemplate<>(producerFactory, replyContainer);
    }

}
