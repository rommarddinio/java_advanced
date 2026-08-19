package com.innowise.apigateway.controller;

import com.innowise.apigateway.dto.request.LoginRequest;
import com.innowise.apigateway.dto.request.RegisterRequest;
import com.innowise.apigateway.dto.response.LoginResponse;
import com.innowise.apigateway.dto.response.RegisterResponse;
import com.innowise.apigateway.service.OrchestratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class RegisterController {

    private final OrchestratorService orchestratorService;

    @PostMapping("/register")
    public Mono<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        return orchestratorService.register(request);
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@RequestBody LoginRequest request) {
        return orchestratorService.login(request);
    }

}
