package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.entity.Credentials;
import com.innowise.authenticationservice.repository.CredentialsRepository;
import com.innowise.authenticationservice.service.CredentialsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialsServiceImpl implements CredentialsService {

    private final CredentialsRepository credentialsRepository;

    @Override
    public void saveCredentials(Credentials credentials) {
        Credentials savedCredentials = credentialsRepository.save(credentials);

        log.info("Successfully saved credentials with id = {}", savedCredentials.getId());
    }

}
