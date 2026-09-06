package com.innowise.paymentservice.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.paymentservice.dto.CreatePaymentEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, CreatePaymentEvent> producerFactory(ObjectMapper objectMapper) {
        Map<String, Object> configProperties = new HashMap<>();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        JsonSerializer<CreatePaymentEvent> jsonSerializer = new JsonSerializer<>(objectMapper);
        jsonSerializer.setAddTypeInfo(false);

        return new DefaultKafkaProducerFactory<>(
                configProperties,
                new StringSerializer(),
                jsonSerializer
        );
    }

    @Bean
    public KafkaTemplate<String, CreatePaymentEvent> kafkaTemplate(
            ProducerFactory<String, CreatePaymentEvent> producerFactory) {

        return new KafkaTemplate<>(producerFactory);
    }
}
