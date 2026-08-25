package com.innowise.apigateway.dto.request;


import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserRegisterRequest (

        @NotEmpty(message = "Name should not be empty")
        @Size(min = 2, max = 50, message = "Name should be between 2 and 50 letters")
        String name,

        @NotEmpty(message = "Surname should not be empty")
        @Size(min = 2, max = 50, message = "Surname should be between 2 and 50 letters")
        String surname,

        @NotNull(message = "Birth date should not be empty")
        @Past(message = "Birth date can't be future")
        LocalDate birthDate,

        @NotBlank(message = "Email should not be empty")
        @Email(message = "Email should be valid")
        String email
){
}
