package com.innowise.userservice.dto.user;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDto {

    @NotEmpty(message = "Name should not be empty")
    @Size(min = 2, max = 50, message = "Name should be between 2 and 50 letters")
    private String name;

    @NotEmpty(message = "Surname should not be empty")
    @Size(min = 2, max = 50, message = "Surname should be between 2 and 50 letters")
    private String surname;

    @NotNull(message = "Birth date should not be empty")
    @Past(message = "Birth date can't be future")
    private LocalDate birthDate;

    @NotBlank(message = "Email should not be empty")
    @Email(message = "Email should be valid")
    private String email;

}
