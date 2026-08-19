package com.innowise.apigateway.dto.response;

public record LoginResponse (

        String accessToken,

        String refreshToken
){
}
