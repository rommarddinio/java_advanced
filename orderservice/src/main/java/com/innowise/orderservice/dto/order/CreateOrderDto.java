package com.innowise.orderservice.dto.order;

import com.innowise.orderservice.dto.orderitem.CreateOrderItemDto;
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

    private String email;

    private List<CreateOrderItemDto> items;

}
