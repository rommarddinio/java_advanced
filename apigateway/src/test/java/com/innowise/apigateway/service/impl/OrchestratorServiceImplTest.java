package com.innowise.apigateway.service.impl;

import com.innowise.apigateway.client.AuthClient;
import com.innowise.apigateway.client.UserClient;
import com.innowise.apigateway.dto.request.LoginRequest;
import com.innowise.apigateway.dto.request.RegisterRequest;
import com.innowise.apigateway.dto.response.LoginResponse;
import com.innowise.apigateway.dto.response.UserResponse;
import com.innowise.apigateway.dto.response.UserStatusResponse;
import com.innowise.apigateway.exception.DeactivatedUserException;
import com.innowise.apigateway.service.TokenService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.function.Function;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrchestratorServiceImplTest {

    @Mock
    private UserClient userClient;

    @Mock
    private AuthClient authClient;

    @Mock
    private TokenService tokenService;

    @Mock
    private ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;

    @Mock
    private RateLimiterRegistry rateLimiterRegistry;

    @InjectMocks
    private OrchestratorServiceImpl orchestratorService;

    RegisterRequest registerRequest;

    UserResponse user;

    String token;

    LoginRequest loginRequest;

    LoginResponse loginResponse;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "Roman",
                "Test",
                LocalDate.of(2000, 1, 1),
                "test@mail.com",
                "login",
                "password"
        );

        user = new UserResponse(
                1L,
                "Roman",
                "Test",
                LocalDate.of(2000, 1, 1),
                "test@mail.com"
        );

        token = "jwt-token";

        loginRequest = new LoginRequest(
                "login",
                "password"
        );

        loginResponse = new LoginResponse(
                "access-token",
                "refresh-token"
        );

        ReactiveCircuitBreaker mockCircuitBreaker = mock(ReactiveCircuitBreaker.class);
        when(circuitBreakerFactory.create(anyString())).thenReturn(mockCircuitBreaker);
        when(mockCircuitBreaker.run(any(Mono.class), any(Function.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RateLimiter mockRateLimiter = mock(RateLimiter.class);
        when(rateLimiterRegistry.rateLimiter(anyString())).thenReturn(mockRateLimiter);
    }

    @Test
    void register_shouldReturnRegisterResponse() {
        when(userClient.createUser(registerRequest))
                .thenReturn(Mono.just(user));

        when(authClient.register(1L, registerRequest))
                .thenReturn(Mono.just(token));

        StepVerifier.create(orchestratorService.register(registerRequest))
                .expectNextMatches(response ->
                        response.user().id().equals(1L)
                                && response.user().name().equals("Roman")
                                && response.token().equals(token)
                )
                .verifyComplete();

        verify(userClient).createUser(registerRequest);
        verify(authClient).register(1L, registerRequest);
        verify(userClient, never()).deleteUser(anyLong());
    }

    @Test
    void register_shouldDeleteUser_whenAuthRegistrationFails() {
        RuntimeException authException = new RuntimeException("Auth service unavailable");

        when(userClient.createUser(registerRequest))
                .thenReturn(Mono.just(user));

        when(authClient.register(1L, registerRequest))
                .thenReturn(Mono.error(authException));

        when(userClient.deleteUser(1L))
                .thenReturn(Mono.empty());

        StepVerifier.create(orchestratorService.register(registerRequest))
                .expectErrorMatches(error -> error == authException)
                .verify();

        verify(userClient).createUser(registerRequest);
        verify(authClient).register(1L, registerRequest);
        verify(userClient).deleteUser(1L);
    }

    @Test
    void register_shouldReturnAuthError_whenRollbackFails() {
        RuntimeException authException = new RuntimeException("Auth failed");

        when(userClient.createUser(registerRequest))
                .thenReturn(Mono.just(user));

        when(authClient.register(1L, registerRequest))
                .thenReturn(Mono.error(authException));

        when(userClient.deleteUser(1L))
                .thenReturn(Mono.error(
                        new RuntimeException("Delete failed")
                ));

        StepVerifier.create(orchestratorService.register(registerRequest))
                .expectErrorMatches(error -> error == authException)
                .verify();

        verify(userClient).deleteUser(1L);
    }

    @Test
    void login_shouldReturnTokens_whenUserIsActive() {
        UserStatusResponse statusResponse = new UserStatusResponse(true);

        when(authClient.login(loginRequest))
                .thenReturn(Mono.just(loginResponse));

        when(tokenService.getUserId("access-token"))
                .thenReturn(1L);

        when(userClient.getUserById(1L))
                .thenReturn(Mono.just(statusResponse));

        StepVerifier.create(orchestratorService.login(loginRequest))
                .expectNext(loginResponse)
                .verifyComplete();

        verify(authClient).login(loginRequest);
        verify(tokenService).getUserId("access-token");
        verify(userClient).getUserById(1L);
    }

    @Test
    void login_shouldThrowException_whenUserIsDeactivated() {
        UserStatusResponse statusResponse = new UserStatusResponse(false);

        when(authClient.login(loginRequest))
                .thenReturn(Mono.just(loginResponse));

        when(tokenService.getUserId("access-token"))
                .thenReturn(1L);

        when(userClient.getUserById(1L))
                .thenReturn(Mono.just(statusResponse));

        StepVerifier.create(orchestratorService.login(loginRequest))
                .expectError(DeactivatedUserException.class)
                .verify();

        verify(authClient).login(loginRequest);
        verify(tokenService).getUserId("access-token");
        verify(userClient).getUserById(1L);
    }
}