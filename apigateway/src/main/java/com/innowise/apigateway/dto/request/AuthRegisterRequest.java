package com.innowise.apigateway.dto.request;

public record AuthRegisterRequest(

        Long userId,

        String login,

        String password
) {
}
