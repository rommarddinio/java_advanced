package com.innowise.apigateway.service.impl;

import com.innowise.apigateway.client.AuthClient;
import com.innowise.apigateway.client.UserClient;
import com.innowise.apigateway.dto.request.LoginRequest;
import com.innowise.apigateway.dto.request.RegisterRequest;
import com.innowise.apigateway.dto.response.LoginResponse;
import com.innowise.apigateway.dto.response.RegisterResponse;
import com.innowise.apigateway.dto.response.UserResponse;
import com.innowise.apigateway.exception.DeactivatedUserException;
import com.innowise.apigateway.exception.ServiceException;
import com.innowise.apigateway.service.OrchestratorService;
import com.innowise.apigateway.service.TokenService;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrchestratorServiceImpl implements OrchestratorService {

    private final UserClient userClient;

    private final AuthClient authClient;

    private final TokenService tokenService;

    private final ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;

    private final RateLimiterRegistry rateLimiterRegistry;

    @Override
    public Mono<RegisterResponse> register(RegisterRequest request) {
        log.info("Request to register user");
        return userClient.createUser(request)
                .flatMap(userResponse -> {
                    Long userId = userResponse.id();
                    log.info("User created successfully with id: {}, proceeding to auth registration", userId);
                    return authClient.register(userId, request)
                            .map(token -> {
                                log.info("Auth registration successful for user id: {}", userId);
                                return new RegisterResponse(
                                        new UserResponse(userId,
                                                userResponse.name(),
                                                userResponse.surname(),
                                                userResponse.birthDate(),
                                                userResponse.email()),
                                        token);
                            })
                            .onErrorResume(authError -> {
                                log.error("Auth registration failed for user id: {}, initiating rollback", userId, authError);
                                return userClient.deleteUser(userId)
                                        .doOnSuccess(v -> log.info("Rollback successful, user id: {} deleted", userId))
                                        .onErrorResume(rollbackError -> {
                                            log.error("Rollback failed for user id: {}", userId, rollbackError);
                                            return Mono.empty();
                                        })
                                        .then(Mono.error(authError));
                            });
                })
                .transform(it -> circuitBreakerFactory.create("registrationCircuit")
                        .run(it, throwable -> handleRegisterFallback(request, throwable)))
                .transformDeferred(RateLimiterOperator.of(rateLimiterRegistry.rateLimiter("registrationRateLimiter")))
                .onErrorResume(RequestNotPermitted.class, e -> handleRegisterRateLimitFallback(request, e));
    }

    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        log.info("Request to login user");
        return authClient.login(request)
                .flatMap(response -> {
                    Long userId = tokenService.getUserId(response.accessToken());
                    log.info("Auth login successful, checking status for user id: {}", userId);
                    return userClient.getUserById(userId)
                            .flatMap(statusResponse -> {
                                if (!statusResponse.active()) {
                                    log.error("Login failed, user id: {} is deactivated", userId);
                                    return Mono.error(new DeactivatedUserException());
                                }
                                log.info("Login successful for user id: {}", userId);
                                return Mono.just(response);
                            });
                })
                .transform(it -> circuitBreakerFactory.create("loginCircuit")
                        .run(it, throwable -> handleLoginCircuitFallback(request, throwable)))
                .transformDeferred(RateLimiterOperator.of(rateLimiterRegistry.rateLimiter("loginRateLimiter")))
                .onErrorResume(RequestNotPermitted.class, e -> handleLoginRateLimitFallback(request, e));
    }

    private Mono<RegisterResponse> handleRegisterFallback(RegisterRequest request, Throwable t) {
        if (t instanceof DeactivatedUserException) {
            throw (DeactivatedUserException) t;
        }
        log.error("Register CircuitBreaker fallback triggered. One of the services might be down.");
        return Mono.error(new ServiceException(HttpStatus.SERVICE_UNAVAILABLE, "Registration failed"));
    }

    private Mono<RegisterResponse> handleRegisterRateLimitFallback(RegisterRequest request, RequestNotPermitted e) {
        log.warn("Registration Rate Limiter triggered! Blocked request for spam protection.");
        return Mono.error(new ServiceException(HttpStatus.TOO_MANY_REQUESTS, "Too many registration attempts. Please try again later"));
    }

    private Mono<LoginResponse> handleLoginCircuitFallback(LoginRequest request, Throwable t) {
        if (t instanceof DeactivatedUserException) {
            throw (DeactivatedUserException) t;
        }
        log.error("Login CircuitBreaker fallback triggered. One of the services might be down.");
        return Mono.error(new ServiceException(HttpStatus.SERVICE_UNAVAILABLE, "Login failed"));
    }

    private Mono<LoginResponse> handleLoginRateLimitFallback(LoginRequest request, RequestNotPermitted e) {
        log.warn("Login Rate Limiter triggered! Blocked request for spam protection.");
        return Mono.error(new ServiceException(HttpStatus.TOO_MANY_REQUESTS, "Too many login attempts. Please try again later"));
    }
}
