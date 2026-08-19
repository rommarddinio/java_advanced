package com.innowise.apigateway.service.impl;

import com.innowise.apigateway.client.AuthClient;
import com.innowise.apigateway.client.UserClient;
import com.innowise.apigateway.dto.request.LoginRequest;
import com.innowise.apigateway.dto.request.RegisterRequest;
import com.innowise.apigateway.dto.response.LoginResponse;
import com.innowise.apigateway.dto.response.RegisterResponse;
import com.innowise.apigateway.dto.response.UserResponse;
import com.innowise.apigateway.exception.DeactivatedUserException;
import com.innowise.apigateway.service.OrchestratorService;
import com.innowise.apigateway.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrchestratorServiceImpl implements OrchestratorService {

    private final UserClient userClient;

    private final AuthClient authClient;

    private final TokenService tokenService;

    @Override
    public Mono<RegisterResponse> register(RegisterRequest request) {
        return userClient.createUser(request)
                .flatMap(userResponse -> {

                    Long userId = userResponse.id();

                    return authClient.register(userId, request)
                            .map(token -> new RegisterResponse(
                                    new UserResponse(userId,
                                            userResponse.name(),
                                            userResponse.surname(),
                                            userResponse.birthDate(),
                                            userResponse.email()),
                                    token
                            ))
                            .onErrorResume(authError ->
                                    userClient.deleteUser(userId)
                                            .onErrorResume(rollbackError -> Mono.empty())
                                            .then(Mono.error(authError))
                            );
                });
    }

    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        return authClient.login(request)
                .flatMap(response ->
                        userClient.getUserById(tokenService.getUserId(response.accessToken()))
                                .flatMap(statusResponse -> {
                                    if (!statusResponse.active()) {
                                        return Mono.error(new DeactivatedUserException());
                                    }
                                    return Mono.just(response);
                                })
                );
    }

}
