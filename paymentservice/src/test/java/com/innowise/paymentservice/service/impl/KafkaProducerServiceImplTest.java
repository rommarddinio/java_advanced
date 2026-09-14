package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.dto.CreatePaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class KafkaProducerServiceImplTest {

    @Mock
    private KafkaTemplate<String, CreatePaymentEvent> kafkaTemplate;

    @InjectMocks
    private KafkaProducerServiceImpl kafkaProducerService;

    private CreatePaymentEvent createPaymentEvent;

    @BeforeEach
    void setUp() {
        createPaymentEvent = new CreatePaymentEvent(1L, "SUCCESS");
    }

    @Test
    void sendToKafka_ShouldSendEventToCorrectTopic_WhenCalled() {
        kafkaProducerService.sendToKafka(createPaymentEvent);

        verify(kafkaTemplate).send("create-payment-topic", createPaymentEvent);
    }
}

