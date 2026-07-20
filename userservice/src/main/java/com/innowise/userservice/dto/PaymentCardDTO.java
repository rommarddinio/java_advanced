package com.innowise.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCardDTO {

    private Long id;

    private Long userId;

    private String number;

    private String holder;

    private LocalDate expirationDate;

    private Boolean active;

}
