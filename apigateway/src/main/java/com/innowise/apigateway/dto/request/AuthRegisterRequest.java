package com.innowise.apigateway.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AuthRegisterRequest(

        @NotNull(message = "User id cannot be null")
        @Positive(message = "User id should be a positive number")
        Long userId,

        @NotBlank(message = "Login should not be empty")
        String login,

        @NotBlank(message = "Password should not be empty")
        String password
) {
}
