package com.innowise.paymentservice.dto;


public record CreatePaymentEvent(

        Long orderId,

        String status

) {
}
