package com.innowise.orderservice.impl;

import com.innowise.orderservice.dto.order.CreateOrderDto;
import com.innowise.orderservice.dto.order.ResponseOrderDto;
import com.innowise.orderservice.dto.orderitem.CreateOrderItemDto;
import com.innowise.orderservice.dto.user.ResponseUserDto;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.enums.Status;
import com.innowise.orderservice.exception.DeletedItemException;
import com.innowise.orderservice.exception.InvalidOrderStatusException;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.UserClientService;
import com.innowise.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserClientService userClientService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;

    private ResponseOrderDto orderDto;

    private CreateOrderDto orderCreateDto;

    private ResponseUserDto userInfo;

    private Item item;

    @BeforeEach
    void setUp() {
        CreateOrderItemDto orderItemCreateDto = new CreateOrderItemDto();
        orderItemCreateDto.setItemId(1L);
        orderItemCreateDto.setQuantity(2L);

        orderCreateDto = new CreateOrderDto();
        orderCreateDto.setEmail("test@example.com");
        orderCreateDto.setItems(List.of(orderItemCreateDto));

        userInfo = new ResponseUserDto();
        userInfo.setId(1L);
        userInfo.setEmail("test@example.com");

        order = new Order();
        order.setId(1L);
        order.setUserId(userInfo.getId());
        order.setStatus(Status.NEW);

        orderDto = new ResponseOrderDto();
        orderDto.setId(order.getId());

        item = new Item();
        item.setId(1L);
        item.setPrice(BigDecimal.valueOf(10));
        item.setDeleted(false);
    }

    @Test
    void createOrder_ShouldReturnOrderDto_WhenSuccessful() {
        when(userClientService.findUserByEmail(orderCreateDto.getEmail())).thenReturn(userInfo);
        when(itemRepository.findAllById(List.of(1L))).thenReturn(List.of(item));
        when(orderMapper.toEntity(orderCreateDto)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(orderRepository.save(order)).thenReturn(order);

        ResponseOrderDto result = orderService.createOrder(orderCreateDto);

        assertNotNull(result);
        assertEquals(orderDto.getId(), result.getId());

        verify(userClientService).findUserByEmail(orderCreateDto.getEmail());
        verify(itemRepository).findAllById(List.of(1L));
        verify(orderRepository).save(order);
    }

    @Test
    void createOrder_ShouldThrowDeletedItemException_WhenItemIsDeleted() {
        item.setDeleted(true);

        when(userClientService.findUserByEmail(orderCreateDto.getEmail())).thenReturn(userInfo);
        when(orderMapper.toEntity(orderCreateDto)).thenReturn(order);
        when(itemRepository.findAllById(List.of(item.getId()))).thenReturn(List.of(item));

        assertThrows(DeletedItemException.class, () -> orderService.createOrder(orderCreateDto));

        verify(userClientService).findUserByEmail(orderCreateDto.getEmail());
        verify(orderMapper).toEntity(orderCreateDto);
        verify(itemRepository).findAllById(List.of(item.getId()));
    }

    @Test
    void createOrder_ShouldThrowItemNotFoundException_WhenItemNotExist() {
        when(userClientService.findUserByEmail(orderCreateDto.getEmail())).thenReturn(userInfo);
        when(orderMapper.toEntity(orderCreateDto)).thenReturn(new Order());
        when(itemRepository.findAllById(List.of(1L))).thenReturn(List.of());

        assertThrows(ItemNotFoundException.class, () -> orderService.createOrder(orderCreateDto));
    }

    @Test
    void updateById_ShouldReturnOrderDto_WhenSuccessful() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userClientService.findUserById(order.getUserId())).thenReturn(userInfo);
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(orderRepository.save(order)).thenReturn(order);

        ResponseOrderDto result = orderService.updateById(1L, Status.PAID);

        assertNotNull(result);
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    @Test
    void updateById_ShouldThrowOrderNotFoundException_WhenNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.updateById(99L, Status.PAID));
    }

    @Test
    void updateById_ShouldThrowInvalidOrderStatusException_WhenStatusInvalid() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userClientService.findUserById(order.getUserId())).thenReturn(userInfo);

        assertThrows(InvalidOrderStatusException.class,
                () -> orderService.updateById(1L, null));
    }

    @Test
    void findById_ShouldReturnOrderDto_WhenSuccessful() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userClientService.findUserById(1L)).thenReturn(userInfo);

        ResponseOrderDto result = orderService.findById(1L);

        assertNotNull(result);
        assertEquals(orderDto.getId(), result.getId());

        verify(orderRepository).findById(1L);
    }

    @Test
    void findById_ShouldThrowOrderNotFoundException_WhenNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.findById(99L));
    }

    @Test
    void deleteById_ShouldCallDelete_WhenSuccessful() {
        when(orderRepository.existsById(1L)).thenReturn(true);

        orderService.deleteById(1L);

        verify(orderRepository).existsById(1L);
        verify(orderRepository).deleteById(1L);
    }

    @Test
    void deleteById_ShouldThrowOrderNotFoundException_WhenNotFound() {
        when(orderRepository.existsById(99L)).thenReturn(false);

        assertThrows(OrderNotFoundException.class,
                () -> orderService.deleteById(99L));

        verify(orderRepository).existsById(99L);
        verify(orderRepository, never()).deleteById(anyLong());
    }

    @Test
    void findByUserId_ShouldReturnOrderDtoList() {
        when(orderRepository.findByUserId(1L)).thenReturn(List.of(order));
        when(userClientService.findUserById(1L)).thenReturn(userInfo);
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        List<ResponseOrderDto> result = orderService.findByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(orderRepository).findByUserId(1L);
        verify(userClientService).findUserById(1L);
    }

    @Test
    void findBySelfId_ShouldReturnOrderDtoList() {
        when(userClientService.findUserBySelfId()).thenReturn(userInfo);
        when(orderRepository.findByUserId(userInfo.getId())).thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderDto);

        List<ResponseOrderDto> result = orderService.findBySelfId();

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(userClientService).findUserBySelfId();
    }

    @Test
    void findAll_ShouldReturnPagedOrders() {
        Page<Order> orderPage = new PageImpl<>(List.of(order), PageRequest.of(0, 10), 1);

        when(orderRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(orderPage);
        when(orderMapper.toDto(order)).thenReturn(orderDto);
        when(userClientService.findAllUsersById(List.of(order.getUserId()))).thenReturn(List.of(userInfo));

        Page<ResponseOrderDto> result = orderService.findAll(PageRequest.of(0,1), Instant.now(), Instant.now(), Status.NEW);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(orderRepository).findAll(any(Specification.class), any(PageRequest.class));
        verify(userClientService).findAllUsersById(List.of(order.getUserId()));
    }
}