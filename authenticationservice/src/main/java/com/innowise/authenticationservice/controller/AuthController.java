package com.innowise.authenticationservice.controller;

import com.innowise.authenticationservice.dto.request.LoginRequest;
import com.innowise.authenticationservice.dto.request.RegisterRequest;
import com.innowise.authenticationservice.dto.response.GeneralResponse;
import com.innowise.authenticationservice.dto.response.LoginResponse;
import com.innowise.authenticationservice.dto.response.TokenPayload;
import com.innowise.authenticationservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<GeneralResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenPayload> validate(@RequestHeader("Authorization") String header) {
        return ResponseEntity.ok(authService.validate(header));
    }

    @PostMapping("/refresh")
    public ResponseEntity<GeneralResponse> refresh(@RequestHeader("Authorization") String header) {
        return ResponseEntity.ok(authService.refresh(header));
    }

}
