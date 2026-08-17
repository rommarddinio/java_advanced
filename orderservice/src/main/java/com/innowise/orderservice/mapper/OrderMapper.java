package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.order.CreateOrderDto;
import com.innowise.orderservice.dto.order.ResponseOrderDto;
import com.innowise.orderservice.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    Order toEntity(CreateOrderDto dto);

    @Mapping(source = "orderItemList", target = "items")
    ResponseOrderDto toDto(Order order);

}
