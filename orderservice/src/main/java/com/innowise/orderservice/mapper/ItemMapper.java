package com.innowise.orderservice.mapper;

import com.innowise.orderservice.dto.item.CreateItemDto;
import com.innowise.orderservice.dto.item.ResponseItemDto;
import com.innowise.orderservice.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "deleted", ignore = true)
    Item toEntity(CreateItemDto createItemDto);

    ResponseItemDto toDto(Item item);

}
