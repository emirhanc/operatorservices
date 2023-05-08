package com.operatorservices.coreservice.config;

import com.operatorservices.coreservice.dto.PurchaseDto;
import com.operatorservices.coreservice.dto.PurchaseOrderDto;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;


@Configuration
public class KafkaConfig {


    @Bean
    public KafkaTemplate<String, PurchaseDto> kafkaTemplate(ProducerFactory<String, PurchaseDto> producerFactory){

        KafkaTemplate<String, PurchaseDto> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setObservationEnabled(true);

        return kafkaTemplate;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PurchaseOrderDto>
    kafkaListenerContainerFactory(ConsumerFactory<String, PurchaseOrderDto> consumerFactory,
                                  KafkaTemplate<String, PurchaseDto> kafkaTemplate){

        ConcurrentKafkaListenerContainerFactory<String, PurchaseOrderDto> concurrentKafkaListenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();

        concurrentKafkaListenerContainerFactory.setConsumerFactory(consumerFactory);
        concurrentKafkaListenerContainerFactory.setReplyTemplate(kafkaTemplate);
        concurrentKafkaListenerContainerFactory.getContainerProperties().setObservationEnabled(true);

        return concurrentKafkaListenerContainerFactory;
    }

    @Bean
    public NewTopic replyTopic() {
        return new NewTopic("reply-topic", 1, (short) 1);
    }

    @Bean
    public NewTopic notificationTopic(){
        return new NewTopic("notification",1, (short) 1);
    }

    @Bean
    public NewTopic zipkinTopic(){ return new NewTopic("zipkin",1, (short) 1); }

}
