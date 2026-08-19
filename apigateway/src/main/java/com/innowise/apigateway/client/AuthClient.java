package com.innowise.apigateway.client;

import com.innowise.apigateway.dto.request.AuthRegisterRequest;
import com.innowise.apigateway.dto.request.LoginRequest;
import com.innowise.apigateway.dto.request.RegisterRequest;
import com.innowise.apigateway.dto.response.LoginResponse;
import com.innowise.apigateway.dto.response.TokenResponse;
import com.innowise.apigateway.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class AuthClient {

    private final WebClient authWebClient;

    public Mono<String> register(Long userId, RegisterRequest request) {
        return authWebClient.post()
                .uri("/auth/register")
                .bodyValue(new AuthRegisterRequest(
                        userId,
                        request.login(),
                        request.password()
                ))
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("Empty error body")
                                .flatMap(body -> Mono.error(
                                        new ServiceException(
                                                HttpStatus.valueOf(response.statusCode().value()),
                                                body
                                        )
                                ))
                )
                .bodyToMono(TokenResponse.class)
                .map(TokenResponse::token);
    }

    public Mono<LoginResponse> login(LoginRequest request) {
        return authWebClient.post()
                .uri("/auth/login")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .defaultIfEmpty("Empty error body")
                                .flatMap(body -> Mono.error(
                                        new ServiceException(
                                                HttpStatus.valueOf(response.statusCode().value()),
                                                body
                                        )
                                ))
                )
                .bodyToMono(LoginResponse.class);
    }

}
