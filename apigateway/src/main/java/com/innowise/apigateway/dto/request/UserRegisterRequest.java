package com.innowise.apigateway.dto.request;


import java.time.LocalDate;

public record UserRegisterRequest (

        String name,

        String surname,

        LocalDate birthDate,

        String email
){
}
