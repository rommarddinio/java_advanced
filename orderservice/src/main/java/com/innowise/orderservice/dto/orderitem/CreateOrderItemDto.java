package com.innowise.orderservice.dto.orderitem;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderItemDto {

    private Long itemId;

    private Long quantity;

}
