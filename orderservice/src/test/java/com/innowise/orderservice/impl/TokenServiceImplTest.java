package com.innowise.orderservice.impl;

import com.innowise.orderservice.service.TokenService;
import com.innowise.orderservice.service.impl.TokenServiceImpl;
import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenServiceImplTest {

    private TokenService tokenService;
    private final String SECRET = "testsecret";

    private String validToken;

    @BeforeEach
    void setUp() {
        tokenService = new TokenServiceImpl();

        ReflectionTestUtils.setField(tokenService, "secret", SECRET);

        validToken = Jwts.builder()
                .setSubject("test-user")
                .claim("userId", 1L)
                .claim("role", "USER")
                .claim("tokenType", "ACCESS")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    @Test
    void getClaimsFromToken_ShouldReturnClaims_WhenTokenIsValid() {
        Claims claims = tokenService.getClaimsFromToken(validToken);

        assertNotNull(claims);
        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals("USER", claims.get("role", String.class));
    }

    @Test
    void getUserId_ShouldReturnUserId_WhenTokenIsValid() {
        Long userId = tokenService.getUserId(validToken);

        assertEquals(1L, userId);
    }

    @Test
    void getRole_ShouldReturnRole_WhenTokenIsValid() {
        String role = tokenService.getRole(validToken);

        assertEquals("USER", role);
    }

    @Test
    void getClaimsFromToken_ShouldThrowException_WhenTokenIsInvalid() {
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

    @Test
    void getClaimsFromToken_ShouldThrowSignatureException_WhenTokenHasWrongSignature() {
        String tokenWithWrongSignature = Jwts.builder()
                .setSubject("test-user")
                .claim("userId", 1L)
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(SignatureAlgorithm.HS256, "wrongsecret")
                .compact();

        assertThrows(SignatureException.class, () -> tokenService.getClaimsFromToken(tokenWithWrongSignature));
    }

    @Test
    void getClaimsFromToken_ShouldThrowUnsupportedJwtException_WhenTokenUnsupported() {
        String unsupportedToken = "eyJhbGciOiJub25lIn0.eyJzdWIiOiJ0ZXN0In0.";
        assertThrows(UnsupportedJwtException.class, () -> tokenService.getClaimsFromToken(unsupportedToken));
    }

    @Test
    void getClaimsFromToken_ShouldThrowIllegalArgumentException_WhenTokenNullOrEmpty() {
        assertThrows(IllegalArgumentException.class, () -> tokenService.getClaimsFromToken(null));
        assertThrows(IllegalArgumentException.class, () -> tokenService.getClaimsFromToken(""));
    }

    @Test
    void getClaimsFromToken_ShouldThrowMalformedJwtException_WhenTokenIsMalformed() {
        String invalidToken = "invalid-token";
        assertThrows(MalformedJwtException.class, () -> tokenService.getClaimsFromToken(invalidToken));
    }

    @Test
    void getTokenType_ShouldReturnTokenType_WhenTokenIsValid() {
        String tokenType = tokenService.getTokenType(validToken);

        assertEquals("ACCESS", tokenType);
    }

    @Test
    void getTokenFromRequest_ShouldReturnToken_WhenHeaderIsValid() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer test-token");

        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);

        String token = tokenService.getTokenFromRequest();

        assertEquals("test-token", token);
    }

    @Test
    void getTokenFromRequest_ShouldReturnNull_WhenHeaderIsMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);

        String token = tokenService.getTokenFromRequest();

        assertNull(token);
    }

    @Test
    void getTokenFromRequest_ShouldReturnNull_WhenHeaderIsNotBearer() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic 123");

        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);

        String token = tokenService.getTokenFromRequest();

        assertNull(token);
    }

    @Test
    void getTokenFromRequest_ShouldThrowException_WhenNoRequestContext() {
        RequestContextHolder.resetRequestAttributes();

        assertThrows(NullPointerException.class, () -> tokenService.getTokenFromRequest());
    }
}