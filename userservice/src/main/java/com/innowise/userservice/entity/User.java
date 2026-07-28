package com.innowise.userservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "users_email_key", columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
public class User extends Auditable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String surname;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String email;

    private Boolean active;

    @OneToMany(mappedBy = "user")
    @BatchSize(size = 5)
    private Set<PaymentCard> paymentCards = new HashSet<>();

}
