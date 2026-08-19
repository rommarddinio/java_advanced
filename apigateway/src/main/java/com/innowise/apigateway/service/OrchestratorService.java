package com.innowise.apigateway.service;

import com.innowise.apigateway.dto.request.LoginRequest;
import com.innowise.apigateway.dto.request.RegisterRequest;
import com.innowise.apigateway.dto.response.LoginResponse;
import com.innowise.apigateway.dto.response.RegisterResponse;
import reactor.core.publisher.Mono;

public interface OrchestratorService {

    Mono<RegisterResponse> register(RegisterRequest request);

    Mono<LoginResponse> login (LoginRequest request);

}
