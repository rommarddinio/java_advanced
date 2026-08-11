package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.order.CreateOrderDto;
import com.innowise.orderservice.dto.order.ResponseOrderDto;
import com.innowise.orderservice.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface OrderService {

    ResponseOrderDto createOrder(CreateOrderDto orderDto);

    ResponseOrderDto findById(Long id);

    List<ResponseOrderDto> findByUserId(Long id);

    List<ResponseOrderDto> findBySelfId();

    Page<ResponseOrderDto> findAll(Pageable pageable, Instant startDate, Instant endDate, Status status);

    void deleteById(Long id);

    ResponseOrderDto updateById(Long id, Status status);
}
