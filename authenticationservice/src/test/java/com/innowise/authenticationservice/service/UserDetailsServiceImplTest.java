package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.details.UserDetailsImpl;
import com.innowise.authenticationservice.entity.Credentials;
import com.innowise.authenticationservice.enums.Role;
import com.innowise.authenticationservice.repository.CredentialsRepository;
import com.innowise.authenticationservice.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private CredentialsRepository credentialsRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        Credentials credentials = new Credentials();
        credentials.setUserId(1L);
        credentials.setLogin("testuser");
        credentials.setPassword("hashedpassword");
        credentials.setRole(Role.ROLE_USER);

        when(credentialsRepository.findByLogin("testuser")).thenReturn(Optional.of(credentials));

        UserDetailsImpl userDetails = userDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals(1L, userDetails.getUserId());
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("ROLE_USER", userDetails.getRole());
        assertEquals("hashedpassword", userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        when(credentialsRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("unknown"));
    }
}
