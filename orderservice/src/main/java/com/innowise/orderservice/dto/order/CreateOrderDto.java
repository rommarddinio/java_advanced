package com.innowise.orderservice.dto.order;

import com.innowise.orderservice.dto.orderitem.CreateOrderItemDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDto {

    @NotBlank(message = "Email should not be empty")
    @Email(message = "Email should be valid")
    private String email;

    private List<CreateOrderItemDto> items;

}
