package com.innowise.orderservice.controller;

import com.innowise.orderservice.dto.order.CreateOrderDto;
import com.innowise.orderservice.dto.order.ResponseOrderDto;
import com.innowise.orderservice.enums.Status;
import com.innowise.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping
    public ResponseEntity<ResponseOrderDto> createOrder(@RequestBody @Valid CreateOrderDto createOrderDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(createOrderDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<ResponseOrderDto> updateById(@PathVariable Long id,
                                                       @RequestBody Status status) {
        return ResponseEntity.ok(orderService.updateById(id, status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseOrderDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<ResponseOrderDto>> findAll(@PageableDefault Pageable pageable,
                                                  @RequestParam(required = false) Instant startDate,
                                                  @RequestParam(required = false) Instant endDate,
                                                  @RequestParam(required = false) Status status) {
        return ResponseEntity.ok(orderService.findAll(pageable, startDate, endDate, status));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ResponseOrderDto>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.findByUserId(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ResponseOrderDto>> findBySelfId() {
        return ResponseEntity.ok(orderService.findBySelfId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        orderService.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
