package com.innowise.orderservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.dto.payment.CreatePaymentEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Value("${kafka.uri}")
    private String kafkaUri;

    @Bean
    public ConsumerFactory<String, CreatePaymentEvent> consumerFactory(
            ObjectMapper objectMapper
    ) {

        Map<String, Object> configProperties = new HashMap<>();
        configProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUri);
        configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "order-service-group");

        JsonDeserializer<CreatePaymentEvent> jsonDeserializer = new JsonDeserializer<>(CreatePaymentEvent.class, objectMapper);

        jsonDeserializer.setUseTypeHeaders(false);
        jsonDeserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                configProperties,
                new StringDeserializer(),
                jsonDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CreatePaymentEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, CreatePaymentEvent> consumerFactory
    ) {
        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, CreatePaymentEvent>();
        containerFactory.setConcurrency(1);
        containerFactory.setConsumerFactory(consumerFactory);

        return containerFactory;
    }
}
