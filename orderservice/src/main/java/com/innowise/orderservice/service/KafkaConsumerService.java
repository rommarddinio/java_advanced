package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.payment.CreatePaymentEvent;

public interface KafkaConsumerService {

    void consumePayment(CreatePaymentEvent createPaymentEvent);

}
