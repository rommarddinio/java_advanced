package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.communication.UserClient;
import com.innowise.orderservice.dto.user.ResponseUserDto;
import com.innowise.orderservice.exception.UserNotFoundException;
import com.innowise.orderservice.service.UserClientService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserClientServiceImpl implements UserClientService {

    private final UserClient userClient;

    private static final String USER_SERVICE = "userServiceCircuit";

    @Retry(name = USER_SERVICE)
    @CircuitBreaker(name = USER_SERVICE, fallbackMethod = "userNotFoundByEmailFallback")
    public ResponseUserDto findUserByEmail(String email) {
        log.info("Requesting user by email via Rest Client");
        try {
            return userClient.findByEmail(email);
        } catch (HttpClientErrorException e) {
            log.error("User with requested email not found in remote service. Error: {}", e.getMessage());
            throw new UserNotFoundException();
        }
    }

    @Retry(name = USER_SERVICE)
    @CircuitBreaker(name = USER_SERVICE, fallbackMethod = "userNotFoundByIdFallback")
    public ResponseUserDto findUserById(Long id) {
        log.info("Requesting user by id: {} via Rest Client", id);
        try {
            return userClient.findById(id);
        } catch (HttpClientErrorException e) {
            log.error("User with id: {} not found in remote service. Error: {}", id, e.getMessage());
            throw new UserNotFoundException();
        }
    }

    @Retry(name = USER_SERVICE)
    @CircuitBreaker(name = USER_SERVICE, fallbackMethod = "userNotFoundBySelfIdFallback")
    public ResponseUserDto findUserBySelfId() {
        log.info("Requesting current user by self id via Rest Client");
        try {
            return userClient.findBySelfId();
        } catch (HttpClientErrorException e) {
            log.error("Current user not found in remote service. Error: {}", e.getMessage());
            throw new UserNotFoundException();
        }
    }

    @Retry(name = USER_SERVICE)
    @CircuitBreaker(name = USER_SERVICE, fallbackMethod = "userListFallback")
    public List<ResponseUserDto> findAllUsersById(List<Long> ids) {
        log.info("Requesting users list by ids: {} via Rest Client", ids);
        try {
            return userClient.findAllById(ids);
        } catch (HttpClientErrorException e) {
            log.error("Failed to fetch users list by ids from remote service. Error: {}", e.getMessage());
            throw new UserNotFoundException();
        }
    }

    private ResponseUserDto userNotFoundByEmailFallback(String email, Throwable t) {
        if (t instanceof UserNotFoundException) {
            throw (UserNotFoundException) t;
        }
        log.error("CircuitBreaker triggered fallback for findUserByEmail. Reason: {}", t.getMessage());
        throw new UserNotFoundException("User service unavailable");
    }

    private ResponseUserDto userNotFoundBySelfIdFallback(Throwable t) {
        if (t instanceof UserNotFoundException) {
            throw (UserNotFoundException) t;
        }
        log.error("CircuitBreaker triggered fallback for findUserBySelfId. Reason: {}", t.getMessage());
        throw new UserNotFoundException("User service unavailable");
    }

    private ResponseUserDto userNotFoundByIdFallback(Long id, Throwable t) {
        if (t instanceof UserNotFoundException) {
            throw (UserNotFoundException) t;
        }
        log.error("CircuitBreaker triggered fallback for findUserById for id: {}. Reason: {}", id, t.getMessage());
        throw new UserNotFoundException("User service unavailable");
    }

    private List<ResponseUserDto> userListFallback(List<Long> ids, Throwable t) {
        if (t instanceof UserNotFoundException) {
            throw (UserNotFoundException) t;
        }
        log.error("CircuitBreaker triggered fallback for userListFallback for ids: {}. Reason: {}", ids, t.getMessage());
        throw new UserNotFoundException("User service unavailable");
    }

}


