package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.details.UserDetailsImpl;
import com.innowise.authenticationservice.dto.request.LoginRequest;
import com.innowise.authenticationservice.dto.request.RegisterRequest;
import com.innowise.authenticationservice.dto.response.GeneralResponse;
import com.innowise.authenticationservice.dto.response.LoginResponse;
import com.innowise.authenticationservice.dto.response.TokenPayload;
import com.innowise.authenticationservice.entity.Credentials;
import com.innowise.authenticationservice.exception.EmptyTokenException;
import com.innowise.authenticationservice.exception.InvalidTokenTypeException;
import com.innowise.authenticationservice.service.impl.AuthServiceImpl;
import com.innowise.authenticationservice.service.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private CredentialsService credentialsService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setLogin("testuser");
        registerRequest.setPassword("password");
        registerRequest.setUserId(1L);

        loginRequest = new LoginRequest();
        loginRequest.setLogin("testuser");
        loginRequest.setPassword("password");
    }

    @Test
    void login_ShouldReturnLoginResponse_WhenCredentialsAreValid() {
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .userId(1L)
                .username("testuser")
                .role("ROLE_USER")
                .build();

        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(tokenService.generateAccessToken(1L, "ROLE_USER")).thenReturn("access-token");
        when(tokenService.generateRefreshToken(1L, "ROLE_USER")).thenReturn("refresh-token");

        LoginResponse response = authService.login(loginRequest);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void register_ShouldReturnGeneralResponse_WithAccessToken() {
        when(passwordEncoder.encode("password")).thenReturn("hashed-password");
        when(tokenService.generateAccessToken(1L, "ROLE_USER")).thenReturn("access-token");

        GeneralResponse response = authService.register(registerRequest);

        assertEquals("access-token", response.getToken());
        verify(credentialsService).saveCredentials(any(Credentials.class));
    }

    @Test
    void validate_ShouldThrowEmptyTokenException_WhenHeaderIsNull() {
        assertThrows(EmptyTokenException.class, () -> authService.validate(null));
    }

    @Test
    void validate_ShouldThrowEmptyTokenException_WhenHeaderInvalid() {
        assertThrows(EmptyTokenException.class, () -> authService.validate("InvalidHeader"));
    }

    @Test
    void validate_ShouldCallGetClaimsFromToken_WhenHeaderValid() {
        String token = "valid-token";

        Claims claims = mock(Claims.class);
        when(claims.get("userId", Long.class)).thenReturn(1L);
        when(claims.get("role", String.class)).thenReturn("ROLE_USER");
        when(claims.get("tokenType", String.class)).thenReturn("ACCESS");
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 300000)); // +5 мин
        when(claims.getIssuedAt()).thenReturn(new Date(System.currentTimeMillis()));

        when(tokenService.getClaimsFromToken(token)).thenReturn(claims);

        TokenPayload payload = authService.validate("Bearer " + token);

        verify(tokenService).getClaimsFromToken(token);
        assertEquals(1L, payload.getUserId());
        assertEquals("ROLE_USER", payload.getRole());
        assertEquals("ACCESS", payload.getTokenType());
        assertNotNull(payload.getExpiration());
        assertNotNull(payload.getIssuedAt());
    }

    @Test
    void refresh_ShouldReturnNewAccessToken_WhenRefreshTokenValid() {
        String refreshToken = "valid-refresh-token";

        when(tokenService.getTokenType(refreshToken)).thenReturn("REFRESH");
        when(tokenService.getUserId(refreshToken)).thenReturn(1L);
        when(tokenService.getRole(refreshToken)).thenReturn("ROLE_USER");
        when(tokenService.generateAccessToken(1L, "ROLE_USER")).thenReturn("new-access-token");

        GeneralResponse response = authService.refresh("Bearer " + refreshToken);

        assertEquals("new-access-token", response.getToken());
        verify(tokenService).getTokenType(refreshToken);
        verify(tokenService, times(2)).getUserId(refreshToken);
        verify(tokenService).getRole(refreshToken);
        verify(tokenService).generateAccessToken(1L, "ROLE_USER");
    }

    @Test
    void refresh_ShouldThrowEmptyTokenException_WhenHeaderInvalid() {
        assertThrows(EmptyTokenException.class, () -> authService.refresh(null));
    }

    @Test
    void refresh_ShouldThrowInvalidTokenTypeException_WhenTokenTypeIsNotRefresh() {
        String token = "access-token";

        when(tokenService.getTokenType(token)).thenReturn("ACCESS");

        InvalidTokenTypeException exception = assertThrows(
                InvalidTokenTypeException.class,
                () -> authService.refresh("Bearer " + token)
        );

        assertNotNull(exception.getMessage());
    }
}
