package com.innowise.orderservice.configuration;

import com.innowise.orderservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    @Value("${services.user.url}")
    private String BASE_URL;

    private final TokenService tokenService;

    @Bean
    public RestClient userRestClient() {
        return RestClient.builder()
                .baseUrl(BASE_URL)
                .requestInterceptor((request, body, execution) -> {
                    String token = tokenService.getTokenFromRequest();
                    if (token != null) {
                        request.getHeaders().set("Authorization", "Bearer " + token);
                    }
                    return execution.execute(request, body);
                })
                .build();
    }

}
