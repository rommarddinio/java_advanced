package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.CreatePaymentEvent;

public interface KafkaProducerService {

    void sendToKafka(CreatePaymentEvent createPaymentEvent);

}
