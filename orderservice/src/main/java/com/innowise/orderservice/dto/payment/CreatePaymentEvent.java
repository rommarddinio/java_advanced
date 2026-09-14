package com.innowise.orderservice.dto.payment;

public record CreatePaymentEvent(
        Long orderId,

        String status
) {
}
