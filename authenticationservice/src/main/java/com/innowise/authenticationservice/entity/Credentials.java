package com.innowise.authenticationservice.entity;

import com.innowise.authenticationservice.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "credentials", uniqueConstraints = {
        @UniqueConstraint(columnNames = "login")
})
@Getter
@Setter
@NoArgsConstructor
public class Credentials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    private String login;

    private String password;

}
