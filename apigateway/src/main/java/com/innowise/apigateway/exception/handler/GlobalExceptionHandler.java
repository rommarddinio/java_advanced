package com.innowise.apigateway.exception.handler;

import com.innowise.apigateway.exception.DeactivatedUserException;
import com.innowise.apigateway.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public Mono<ResponseEntity<String>> handleServicerException(ServiceException e) {

        return Mono.just(
                ResponseEntity
                        .status(e.getStatus())
                        .body(e.getMessage())
        );
    }

    @ExceptionHandler(DeactivatedUserException.class)
    public Mono<ResponseEntity<String>> handleDeactivatedUserException(DeactivatedUserException e) {

        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(e.getMessage())
        );
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<List<String>>> handleValidationExceptions(WebExchangeBindException e) {

        List<String> errors = e.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .toList();

        return Mono.just(new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST));
    }
}
