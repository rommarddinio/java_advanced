package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.service.impl.TokenServiceImpl;
import io.jsonwebtoken.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceImplTest {

    private TokenService tokenService;

    private final String SECRET = "testsecret";

    private final Duration ACCESS_LIFETIME = Duration.ofMillis(100000);

    private final Duration REFRESH_LIFETIME = Duration.ofMillis(200000);

    private String accessToken;

    private String refreshToken;

    @BeforeEach
    void setUp() {
        tokenService = new TokenServiceImpl();

        ReflectionTestUtils.setField(tokenService, "secret", SECRET);
        ReflectionTestUtils.setField(tokenService, "accessLifetime", ACCESS_LIFETIME);
        ReflectionTestUtils.setField(tokenService, "refreshLifetime", REFRESH_LIFETIME);

        accessToken = tokenService.generateAccessToken(1L, "USER");
        refreshToken = tokenService.generateRefreshToken(1L, "USER");
    }

    @Test
    void generateAccessToken_ShouldReturnNonNullToken() {
        assertNotNull(accessToken);
        assertFalse(accessToken.isEmpty());
    }

    @Test
    void generateRefreshToken_ShouldReturnNonNullToken() {
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
    }

    @Test
    void getClaimsFromToken_ShouldReturnClaims_WhenTokenIsValid() {
        Claims claims = tokenService.getClaimsFromToken(accessToken);
        assertNotNull(claims);
        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals("USER", claims.get("role", String.class));
    }

    @Test
    void getUserId_ShouldReturnUserId_WhenTokenIsValid() {
        Long userId = tokenService.getUserId(accessToken);
        assertEquals(1L, userId);
    }

    @Test
    void getRole_ShouldReturnRole_WhenTokenIsValid() {
        String role = tokenService.getRole(accessToken);
        assertEquals("USER", role);
    }

    @Test
    void getClaimsFromToken_ShouldThrowException_WhenTokenIsMalformed() {
        String invalidToken = "invalid";
        assertThrows(MalformedJwtException.class, () -> tokenService.getClaimsFromToken(invalidToken));
    }

    @Test
    void getClaimsFromToken_ShouldThrowException_WhenTokenIsExpired() {
        String expiredToken = Jwts.builder()
                .setSubject("test-user")
                .claim("userId", 1L)
                .claim("role", "USER")
                .setIssuedAt(new Date(System.currentTimeMillis() - 200000))
                .setExpiration(new Date(System.currentTimeMillis() - 100000))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> tokenService.getClaimsFromToken(expiredToken));
    }
}
