package com.innowise.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "random-number", url = "${feign.random-number.url}",
        fallback = RandomNumberClientFallback.class)
public interface RandomNumberClient {

    @GetMapping
    String getNumber();

}
