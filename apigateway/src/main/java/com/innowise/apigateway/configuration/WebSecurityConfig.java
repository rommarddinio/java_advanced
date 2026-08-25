package com.innowise.apigateway.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@SuppressWarnings("java:S4502")
public class WebSecurityConfig {

    private final AuthenticationManager authenticationManager;

    private final SecurityContextRepository securityContextRepository;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {

        return http
                .exceptionHandling((exceptionHandlingSpec ->
                        exceptionHandlingSpec .authenticationEntryPoint((swe, e) ->
                                Mono.fromRunnable( () -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED) ))))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/register").permitAll()
                        .pathMatchers("/login").permitAll()
                        .anyExchange().authenticated()
                )
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .build();
    }
}
