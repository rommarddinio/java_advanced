package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.dto.CreatePaymentEvent;
import com.innowise.paymentservice.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, CreatePaymentEvent> kafkaTemplate;

    @Override
    public void sendToKafka(CreatePaymentEvent createPaymentEvent) {
        kafkaTemplate.send("create-payment-topic", createPaymentEvent);
        log.info("Create payment event is sent to Kafka with order id = {}", createPaymentEvent.orderId());
    }
}
