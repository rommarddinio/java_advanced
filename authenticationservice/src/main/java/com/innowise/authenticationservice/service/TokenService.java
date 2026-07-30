package com.innowise.authenticationservice.service;

import io.jsonwebtoken.Claims;

public interface TokenService {

    String generateAccessToken(Long userId, String role);

    String generateRefreshToken(Long userId, String role);

    Claims getClaimsFromToken(String token);

    Long getUserId(String token);

    String getRole(String token);

    String getTokenType(String token);

}
