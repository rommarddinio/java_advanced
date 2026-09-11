package com.innowise.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreatePaymentDto (

        @NotNull(message = "Order id can't be null")
        @Positive(message = "Order id must be greater than 0")
        Long orderId,

        @NotNull(message = "Payment amount can't be null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal paymentAmount

) {
}
