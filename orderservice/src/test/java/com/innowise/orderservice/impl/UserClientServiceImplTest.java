package com.innowise.orderservice.impl;

import com.innowise.orderservice.communication.UserClient;
import com.innowise.orderservice.dto.user.ResponseUserDto;
import com.innowise.orderservice.exception.UserNotFoundException;
import com.innowise.orderservice.service.impl.UserClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserClientServiceImplTest {

    @Mock
    private UserClient userClient;

    @InjectMocks
    private UserClientServiceImpl userClientService;

    private ResponseUserDto user;

    @BeforeEach
    void setUp() {
        user = new ResponseUserDto();
        user.setId(1L);
        user.setEmail("test@mail.com");
    }

    @Test
    void findUserByEmail_ShouldReturnUser_WhenSuccessful() {
        when(userClient.findByEmail(user.getEmail())).thenReturn(user);

        ResponseUserDto result = userClientService.findUserByEmail(user.getEmail());

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());

        verify(userClient).findByEmail(user.getEmail());
    }

    @Test
    void findUserById_ShouldReturnUser_WhenSuccessful() {
        when(userClient.findById(user.getId())).thenReturn(user);

        ResponseUserDto result = userClientService.findUserById(user.getId());

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());

        verify(userClient).findById(user.getId());
    }

    @Test
    void findUserBySelfId_ShouldReturnUser_WhenSuccessful() {
        when(userClient.findBySelfId()).thenReturn(user);

        ResponseUserDto result = userClientService.findUserBySelfId();

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());

        verify(userClient).findBySelfId();
    }

    @Test
    void findAllUsersById_ShouldReturnList_WhenSuccessful() {
        List<ResponseUserDto> users = List.of(new ResponseUserDto(), new ResponseUserDto());

        when(userClient.findAllById(List.of(1L, 2L))).thenReturn(users);

        List<ResponseUserDto> result = userClientService.findAllUsersById(List.of(1L, 2L));

        assertNotNull(result);
        assertEquals(2, result.size());

        verify(userClient).findAllById(List.of(1L, 2L));
    }

    @Test
    void findUserByEmail_ShouldThrowException_WhenNotFound() {
        when(userClient.findByEmail(user.getEmail()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(UserNotFoundException.class,
                () -> userClientService.findUserByEmail(user.getEmail()));

        verify(userClient).findByEmail(user.getEmail());
    }

    @Test
    void findUserById_ShouldThrowException_WhenNotFound() {
        when(userClient.findById(user.getId()))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(UserNotFoundException.class,
                () -> userClientService.findUserById(user.getId()));

        verify(userClient).findById(user.getId());
    }

    @Test
    void findUserBySelfId_ShouldThrowException_WhenNotFound() {
        when(userClient.findBySelfId())
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(UserNotFoundException.class,
                () -> userClientService.findUserBySelfId());

        verify(userClient).findBySelfId();
    }

    @Test
    void findAllUsersById_ShouldThrowException_WhenNotFound() {
        when(userClient.findAllById(List.of(1L, 2L)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertThrows(UserNotFoundException.class,
                () -> userClientService.findAllUsersById(List.of(1L, 2L)));

        verify(userClient).findAllById(List.of(1L, 2L));
    }
}