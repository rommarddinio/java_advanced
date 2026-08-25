package com.innowise.apigateway.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    @Value("${services.auth.url}")
    private String AUTH_URL;

    @Value("${services.user.url}")
    private String USER_URL;

    @Bean
    public WebClient authWebClient() {
        return WebClient.builder()
                .baseUrl(AUTH_URL)
                .build();
    }

    @Bean
    public WebClient userWebClient() {
        return WebClient.builder()
                .baseUrl(USER_URL)
                .build();
    }
}
