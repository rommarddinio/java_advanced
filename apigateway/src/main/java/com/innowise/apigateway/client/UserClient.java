package com.innowise.apigateway.client;

import com.innowise.apigateway.dto.request.RegisterRequest;
import com.innowise.apigateway.dto.request.UserRegisterRequest;
import com.innowise.apigateway.dto.response.UserResponse;
import com.innowise.apigateway.dto.response.UserStatusResponse;
import com.innowise.apigateway.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final WebClient userWebClient;

    @Value("${internal.secret}")
    private String secret;

    public Mono<UserResponse> createUser(RegisterRequest request) {
        return userWebClient.post()
                .uri("/users")
                .bodyValue(new UserRegisterRequest(
                        request.name(),
                        request.surname(),
                        request.birthDate(),
                        request.email()
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
                .bodyToMono(UserResponse.class);
    }

    public Mono<Void> deleteUser(Long userId) {
        return userWebClient.delete()
                .uri("/users/{id}", userId)
                .header("X-Service-Secret", secret)
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
                .bodyToMono(Void.class);
    }

    public Mono<UserStatusResponse> getUserById(Long userId) {
        return userWebClient.get()
                .uri("/users/{id}", userId)
                .header("X-Service-Secret", secret)
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
                .bodyToMono(UserStatusResponse.class);
    }



}
