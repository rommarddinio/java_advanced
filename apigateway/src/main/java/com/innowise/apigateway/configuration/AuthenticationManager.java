package com.innowise.apigateway.configuration;

import com.innowise.apigateway.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final TokenService tokenService;

    @Override
    public Mono<Authentication> authenticate(Authentication auth) {

        String token = auth.getCredentials().toString();

        try {
            if (!"ACCESS".equals(tokenService.getTokenType(token))) {
                return Mono.empty();
            }

            Long userId = tokenService.getUserId(token);
            String role = tokenService.getRole(token);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );

            return Mono.just(authentication);

        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
