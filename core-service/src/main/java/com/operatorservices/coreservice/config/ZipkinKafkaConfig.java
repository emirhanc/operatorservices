package com.operatorservices.coreservice.config;

import java.util.List;
import java.util.Map;

import org.apache.kafka.common.serialization.ByteArraySerializer;
import zipkin2.reporter.Sender;
import zipkin2.reporter.kafka.KafkaSender;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ByteArraySerializer.class)
public class ZipkinKafkaConfig {

    private static final String SENDER_BEAN_NAME = "zipkinSender";


    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(KafkaProperties.class)
    static class ZipkinKafkaBeanConfiguration {

        static String join(List<?> parts) {
            StringBuilder to = new StringBuilder();
            for (int i = 0, length = parts.size(); i < length; i++) {
                to.append(parts.get(i));
                if (i + 1 < length) {
                    to.append(',');
                }
            }
            return to.toString();
        }

        @Bean(SENDER_BEAN_NAME)
        Sender kafkaSender(KafkaProperties config) {

            String topic = "zipkin";
            Map<String, Object> properties = config.buildProducerProperties();
            properties.put("key.serializer", ByteArraySerializer.class.getName());
            properties.put("value.serializer", ByteArraySerializer.class.getName());

            Object bootstrapServers = properties.get("bootstrap.servers");
            if (bootstrapServers instanceof List) {
                properties.put("bootstrap.servers", join((List) bootstrapServers));
            }

            return KafkaSender.newBuilder().topic(topic).overrides(properties).build();
        }

    }

}