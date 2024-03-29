package com.operatorservices.purchaseorderservice.config;

import com.operatorservices.purchaseorderservice.dto.PurchaseOrderDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
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

        //replyContainer.getContainerProperties().setObservationEnabled(true);

        ReplyingKafkaTemplate<String, PurchaseOrderDto, Object> replyingKafkaTemplate =
                new ReplyingKafkaTemplate<>(producerFactory, replyContainer);
        replyingKafkaTemplate.setObservationEnabled(true);

        return replyingKafkaTemplate;
    }

    //Following bean does not make any difference in terms of observation as of Spring Boot 3.0.6
/*    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory
    ){
        ConcurrentKafkaListenerContainerFactory<String, Object> concurrentKafkaListenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();

        concurrentKafkaListenerContainerFactory.setConsumerFactory(consumerFactory);
        concurrentKafkaListenerContainerFactory.getContainerProperties().setObservationEnabled(true);

        return concurrentKafkaListenerContainerFactory;
    }*/

    @Bean
    public NewTopic replyTopic() {
        return new NewTopic(replyTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic purchaseOrderTopic() {
        return new NewTopic("purchase-order", 2, (short) 1);
    }

    @Bean
    public NewTopic zipkinTopic(){
        return new NewTopic("zipkin",1, (short) 1);
    }
}
