package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.orderitem.CreateOrderItemDto;
import com.innowise.orderservice.dto.orderitem.ResponseOrderItemDto;
import com.innowise.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "item", target = "itemDto")
    ResponseOrderItemDto toDto(OrderItem orderItem);

    OrderItem toEntity(CreateOrderItemDto dto);

}
