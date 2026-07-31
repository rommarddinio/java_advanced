package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.details.UserDetailsImpl;
import com.innowise.authenticationservice.dto.request.LoginRequest;
import com.innowise.authenticationservice.dto.request.RegisterRequest;
import com.innowise.authenticationservice.dto.response.GeneralResponse;
import com.innowise.authenticationservice.dto.response.LoginResponse;
import com.innowise.authenticationservice.dto.response.TokenPayload;
import com.innowise.authenticationservice.entity.Credentials;
import com.innowise.authenticationservice.enums.Role;
import com.innowise.authenticationservice.enums.TokenType;
import com.innowise.authenticationservice.exception.EmptyTokenException;
import com.innowise.authenticationservice.exception.InvalidTokenTypeException;
import com.innowise.authenticationservice.service.AuthService;
import com.innowise.authenticationservice.service.CredentialsService;
import com.innowise.authenticationservice.service.TokenService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final CredentialsService credentialsService;

    private final UserDetailsServiceImpl userDetailsService;

    private final AuthenticationManager authenticationManager;

    private final TokenService tokenService;

    private final BCryptPasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getLogin());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getLogin(), request.getPassword()));

        UserDetailsImpl userDetails = userDetailsService.loadUserByUsername(request.getLogin());

        log.info("User {} authenticated successfully. Generating tokens.", request.getLogin());
        return new LoginResponse(tokenService.generateAccessToken(userDetails.getUserId(), userDetails.getRole()),
                tokenService.generateRefreshToken(userDetails.getUserId(), userDetails.getRole()));
    }

    public GeneralResponse register(RegisterRequest request) {
        log.info("Registering new user with ID: {} and login: {}", request.getUserId(), request.getLogin());
        Credentials credentials = new Credentials();
        credentials.setUserId(request.getUserId());
        credentials.setRole(Role.ROLE_USER);
        credentials.setLogin(request.getLogin());
        credentials.setPassword(passwordEncoder.encode(request.getPassword()));

        credentialsService.saveCredentials(credentials);

        log.info("User with login: {} registered successfully.", request.getLogin());
        return new GeneralResponse(tokenService.generateAccessToken(request.getUserId(),
                Role.ROLE_USER.name()));
    }

    public TokenPayload validate(String header) {
        log.info("Validating token from authorization header");
        String token = extractToken(header);

        Claims claims = tokenService.getClaimsFromToken(token);

        log.info("Token validated successfully for userId: {}", claims.get("userId", Long.class));
        return TokenPayload.builder()
                .userId(claims.get("userId", Long.class))
                .role(claims.get("role",String.class))
                .tokenType(claims.get("tokenType", String.class))
                .expiration(claims.getExpiration())
                .issuedAt(claims.getIssuedAt())
                .build();
    }

    public GeneralResponse refresh(String header) {
        log.info("Processing token refresh request");
        String token = extractToken(header);

        if (!tokenService.getTokenType(token).equals(TokenType.REFRESH.name())) {
            log.warn("Refresh token validation failed: invalid token type");
            throw new InvalidTokenTypeException();
        }

        String newAccessToken = tokenService.generateAccessToken(
                tokenService.getUserId(token), tokenService.getRole(token));

        log.info("Access token successfully refreshed for userId: {}", tokenService.getUserId(token));
        return new GeneralResponse(newAccessToken);
    }

    private String extractToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("Token extraction failed: header is missing or does not start with Bearer");
            throw new EmptyTokenException();
        }
        return header.substring(7);
    }

}