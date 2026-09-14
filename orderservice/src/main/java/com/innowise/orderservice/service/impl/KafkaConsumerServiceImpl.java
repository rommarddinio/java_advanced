package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.dto.payment.CreatePaymentEvent;
import com.innowise.orderservice.enums.Status;
import com.innowise.orderservice.service.KafkaConsumerService;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    private final OrderService orderService;

    @KafkaListener(topics = "create-payment-topic", groupId = "order-service-group")
    @Override
    public void consumePayment(CreatePaymentEvent createPaymentEvent) {
        log.info("Received create payment event with order id = {}", createPaymentEvent.orderId());

        if(!orderService.existsById(createPaymentEvent.orderId())) {
            log.warn("There is no order with id = {}", createPaymentEvent.orderId());
            return;
        }

        log.info("Create payment event is received with order id = {}", createPaymentEvent.orderId());

        Status status = createPaymentEvent.status().equals("SUCCESS") ? Status.PAID :
                Status.CANCELLED;

        orderService.updateById(createPaymentEvent.orderId(), status);

        log.info("Order with id = {} has been updated with status = {}", createPaymentEvent.orderId(),
                status.name());
    }
}
