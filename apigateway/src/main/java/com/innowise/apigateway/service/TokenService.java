package com.innowise.apigateway.service;

import io.jsonwebtoken.Claims;

public interface TokenService {

    Claims getClaimsFromToken(String token);

    Long getUserId(String token);

    String getRole(String role);

    String getTokenType(String token);

}
