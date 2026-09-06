package com.innowise.paymentservice.dto;

import com.innowise.paymentservice.enums.Status;

import java.math.BigDecimal;
import java.time.Instant;

public record ResponsePaymentDto(

        String id,

        Long userId,

        Long orderId,

        Status status,

        Instant timestamp,

        BigDecimal paymentAmount

) {
}
