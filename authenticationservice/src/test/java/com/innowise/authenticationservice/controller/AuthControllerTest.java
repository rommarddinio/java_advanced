package com.innowise.authenticationservice.controller;

import com.innowise.authenticationservice.details.UserDetailsImpl;
import com.innowise.authenticationservice.dto.response.LoginResponse;
import com.innowise.authenticationservice.dto.request.RegisterRequest;
import com.innowise.authenticationservice.entity.Credentials;
import com.innowise.authenticationservice.enums.Role;
import com.innowise.authenticationservice.repository.CredentialsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"JWT_SECRET=testsecret",
        "JWT_ACCESS_TIME=5m",
        "JWT_REFRESH_TIME=3d"})
class AuthControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
            .withDatabaseName("authdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CredentialsRepository credentialsRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private RegisterRequest request;

    private UserDetailsImpl user;

    @BeforeEach
    void setUp() {
        credentialsRepository.deleteAll();

        user = new UserDetailsImpl(1L, "testuser",
                "password", Role.ROLE_USER.name());

        Credentials credentials = new Credentials();
        credentials.setUserId(1L);
        credentials.setLogin("testuser");
        credentials.setPassword(passwordEncoder.encode("password"));
        credentials.setRole(Role.ROLE_USER);
        credentialsRepository.save(credentials);

        request = new RegisterRequest();
        request.setUserId(1L);
        request.setLogin("testuser");
        request.setPassword("password");
    }

    @Test
    void register_ShouldReturnAccessToken_WhenSuccessful() throws Exception {
        request.setLogin("bebra");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_ShouldReturnAccessAndRefreshToken_WhenCredentialsValid() throws Exception {

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void validate_ShouldReturn200_WhenTokenValid() throws Exception {
        LoginResponse loginResponse = objectMapper.readValue(
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andReturn().getResponse().getContentAsString(),
                LoginResponse.class
        );

        mockMvc.perform(post("/auth/validate")
                        .with(user(user))
                        .header("Authorization", "Bearer " + loginResponse.getAccessToken()))
                .andExpect(status().isOk());
    }

    @Test
    void refresh_ShouldReturnNewAccessToken_WhenTokenValid() throws Exception {

        LoginResponse loginResponse = objectMapper.readValue(
                mockMvc.perform(post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andReturn().getResponse().getContentAsString(),
                LoginResponse.class
        );

        mockMvc.perform(post("/auth/refresh")
                        .with(user(user))
                        .header("Authorization", "Bearer " + loginResponse.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }


    @Test
    void refresh_ShouldReturn401_WhenHeaderInvalid() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .header("Authorization", "InvalidHeader"))
                .andExpect(status().isUnauthorized());
    }
}