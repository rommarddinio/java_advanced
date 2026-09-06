package com.innowise.paymentservice.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;

import java.time.Instant;

public record TimeRangeDto(

        @NotNull(message = "Time limits can't be null")
        @Past(message = "The first time limit must be past")
        Instant from,

        @NotNull(message = "Time limits can't be null")
        @PastOrPresent (message = "The second time limit must be past or present")
        Instant to
) {

    @AssertTrue(message = "The 'from' timestamp must be strictly before the 'to' timestamp")
    public boolean isValidRange() {
        return from.isBefore(to);
    }
}
