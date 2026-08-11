package com.innowise.authenticationservice.service;

import com.innowise.authenticationservice.entity.Credentials;
import com.innowise.authenticationservice.enums.Role;
import com.innowise.authenticationservice.repository.CredentialsRepository;
import com.innowise.authenticationservice.service.impl.CredentialsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialsServiceImplTest {

    @Mock
    private CredentialsRepository credentialsRepository;

    @InjectMocks
    private CredentialsServiceImpl credentialsService;

    @Test
    void saveCredentials_ShouldCallRepositorySaveAndLogSuccess() {
        Credentials credentials = new Credentials();
        credentials.setId(67L);
        credentials.setUserId(1L);
        credentials.setLogin("testuser");
        credentials.setPassword("hashedpassword");

        when(credentialsRepository.save(credentials)).thenReturn(credentials);

        credentialsService.saveCredentials(credentials);

        verify(credentialsRepository).save(credentials);
    }
}
