package com.innowise.orderservice.dto.order;

import com.innowise.orderservice.dto.orderitem.ResponseOrderItemDto;
import com.innowise.orderservice.dto.user.ResponseUserDto;
import com.innowise.orderservice.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseOrderDto {

    private Long id;

    private ResponseUserDto user;

    private Status status;

    private BigDecimal totalPrice;

    private List<ResponseOrderItemDto> items;

    private Boolean deleted;

}
