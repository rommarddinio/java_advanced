package com.innowise.apigateway.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Login should not be empty")
        String login,

        @NotBlank(message = "Password should not be empty")
        String password
) {
}
