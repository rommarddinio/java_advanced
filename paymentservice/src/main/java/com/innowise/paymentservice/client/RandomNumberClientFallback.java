package com.innowise.paymentservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RandomNumberClientFallback implements RandomNumberClient{

    @Override
    public String getNumber() {
        log.warn("External API is down! Executing fallback logic.");
        return "1";
    }

}
