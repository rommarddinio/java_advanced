package com.innowise.userservice.repository;

import com.innowise.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Modifying
    @Query("UPDATE User SET User.active = :active WHERE User.id = :id")
    void setActive(Long id, Boolean active);

    Optional<User> findByEmail(String email);

}
