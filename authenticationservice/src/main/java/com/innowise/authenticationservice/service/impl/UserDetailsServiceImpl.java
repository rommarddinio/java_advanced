package com.innowise.authenticationservice.service.impl;

import com.innowise.authenticationservice.details.UserDetailsImpl;
import com.innowise.authenticationservice.entity.Credentials;
import com.innowise.authenticationservice.repository.CredentialsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CredentialsRepository credentialsRepository;

    @Override
    public UserDetailsImpl loadUserByUsername(String login) throws UsernameNotFoundException {
        Credentials credentials = credentialsRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(String
                        .format("User with login %s is not found", login)));

        return UserDetailsImpl.builder()
                .userId(credentials.getUserId())
                .username(credentials.getLogin())
                .role(credentials.getRole().toString())
                .password(credentials.getPassword())
                .build();
    }
}
