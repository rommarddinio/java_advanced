package com.innowise.authenticationservice.repository;

import com.innowise.authenticationservice.entity.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
}
