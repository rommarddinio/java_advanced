package com.innowise.apigateway.controller;

import com.innowise.apigateway.dto.request.LoginRequest;
import com.innowise.apigateway.dto.request.RegisterRequest;
import com.innowise.apigateway.dto.response.LoginResponse;
import com.innowise.apigateway.dto.response.RegisterResponse;
import com.innowise.apigateway.dto.response.UserResponse;
import com.innowise.apigateway.service.OrchestratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "JWT_SECRET=testsecret",
        "SERVICE_SECRET=secret",
        "USER_SERVICE=http://userservice",
        "AUTH_SERVICE=http://authservice",
        "ORDER_SERVICE=http://orderservice",
        "PAYMENT_SERVICE=http://paymentservice"
})
@AutoConfigureWebTestClient
class RegistrationControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrchestratorService orchestratorService;

    @Test
    void register_shouldReturnRegisterResponse() {

        RegisterResponse response = new RegisterResponse(
                new UserResponse(
                        1L,
                        "Roman",
                        "Sidorchuk",
                        LocalDate.of(2005, 1, 1),
                        "roman@gmail.com"
                ),
                "access-token"
        );

        when(orchestratorService.register(any(RegisterRequest.class)))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "name": "Roman",
                            "surname": "Sidorchuk",
                            "birthDate": "2005-01-01",
                            "email": "roman@gmail.com",
                            "login": "roman",
                            "password": "password123"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.user.id").isEqualTo(1)
                .jsonPath("$.user.name").isEqualTo("Roman")
                .jsonPath("$.user.surname").isEqualTo("Sidorchuk")
                .jsonPath("$.user.email").isEqualTo("roman@gmail.com")
                .jsonPath("$.token").isEqualTo("access-token");
    }

    @Test
    void login_shouldReturnLoginResponse() {

        LoginResponse response = new LoginResponse(
                "access-token",
                "refresh-token"
        );

        when(orchestratorService.login(any(LoginRequest.class)))
                .thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "login": "roman",
                            "password": "password123"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("access-token")
                .jsonPath("$.refreshToken").isEqualTo("refresh-token");
    }
}