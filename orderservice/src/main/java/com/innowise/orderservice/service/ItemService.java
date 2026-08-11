package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.item.CreateItemDto;
import com.innowise.orderservice.dto.item.ResponseItemDto;
import com.innowise.orderservice.dto.item.UpdateItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemService {

    ResponseItemDto createItem(CreateItemDto createItemDto);

    ResponseItemDto updateItem(Long id, UpdateItemDto itemDto);

    ResponseItemDto findById(Long id);

    Page<ResponseItemDto> findAll(Pageable pageable);

    void deleteById(Long id);

}
