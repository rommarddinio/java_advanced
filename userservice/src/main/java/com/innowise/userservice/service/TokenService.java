package com.innowise.userservice.service;

import io.jsonwebtoken.Claims;

public interface TokenService {

    Claims getClaimsFromToken(String token);

    Long getUserId(String token);

    String getRole(String token);

    String getTokenType(String token);
}
