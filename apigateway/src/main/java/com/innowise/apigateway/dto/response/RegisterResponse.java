package com.innowise.apigateway.dto.response;

public record RegisterResponse(

        UserResponse user,

        String token
) {
}