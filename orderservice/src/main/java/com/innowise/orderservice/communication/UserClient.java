package com.innowise.orderservice.communication;

import com.innowise.orderservice.dto.user.ResponseUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestClient restClient;

    public ResponseUserDto findByEmail(String email) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("email", email)
                        .build())
                .retrieve()
                .body(ResponseUserDto.class);
    }

    public ResponseUserDto findById(Long id) {
        return restClient.get()
                .uri("/{id}", id)
                .retrieve()
                .body(ResponseUserDto.class);
    }

    public ResponseUserDto findBySelfId() {
        return restClient.get()
                .uri("/me")
                .retrieve()
                .body(ResponseUserDto.class);
    }

    public List<ResponseUserDto> findAllById(List<Long> ids) {
        return restClient.post()
                .uri("/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ids)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
