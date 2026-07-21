package com.innowise.userservice.dto.paymentcard;

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
public class UpdatePaymentCardDto {

    @NotNull(message = "User ID is required")
    @Min(value = 1, message = "User ID must be greater than 0")
    private Long userId;

    @NotEmpty(message = "Number should not be empty")
    @Pattern(regexp = "\\d{4} \\d{4} \\d{4} \\d{4}",
            message = "Number must be in format XXXX XXXX XXXX XXXX")
    private String number;

    @NotEmpty(message = "Holder can not be empty")
    @Pattern(regexp = "[A-Z]+ [A-Z]+",
            message = "Holder must contain only uppercase letters separated by spaces")
    private String holder;

    @NotNull(message = "Expiration date should not be empty")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;

}
