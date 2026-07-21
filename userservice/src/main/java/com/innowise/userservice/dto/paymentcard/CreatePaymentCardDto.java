package com.innowise.userservice.dto.paymentcard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentCardDto {

    private Long userId;

    private String number;

    private String holder;

    private LocalDate expirationDate;

}
