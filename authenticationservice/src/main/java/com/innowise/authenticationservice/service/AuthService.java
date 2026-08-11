package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.dto.request.LoginRequest;
import com.innowise.authenticationservice.dto.request.RegisterRequest;
import com.innowise.authenticationservice.dto.response.GeneralResponse;
import com.innowise.authenticationservice.dto.response.LoginResponse;
import com.innowise.authenticationservice.dto.response.TokenPayload;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    GeneralResponse register(RegisterRequest request);

    TokenPayload validate(String header);

    GeneralResponse refresh(String header);

}