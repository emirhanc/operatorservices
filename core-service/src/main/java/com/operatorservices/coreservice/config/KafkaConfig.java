package com.operatorservices.coreservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic replyTopic() {
        return new NewTopic("reply-topic", 1, (short) 1);
    }

    @Bean
    public NewTopic notificationTopic(){
        return new NewTopic("notification",1, (short) 1);
    }

    @Bean
    public NewTopic zipkinTopic(){
        return new NewTopic("zipkin",1, (short) 1);
    }

}
