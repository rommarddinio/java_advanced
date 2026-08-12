package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.dto.order.CreateOrderDto;
import com.innowise.orderservice.dto.order.ResponseOrderDto;
import com.innowise.orderservice.dto.orderitem.CreateOrderItemDto;
import com.innowise.orderservice.dto.user.ResponseUserDto;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.entity.OrderItem;
import com.innowise.orderservice.enums.Status;
import com.innowise.orderservice.exception.DeletedItemException;
import com.innowise.orderservice.exception.InvalidOrderStatusException;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.exception.OrderNotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.service.UserClientService;
import com.innowise.orderservice.specifications.OrderSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final ItemRepository itemRepository;

    private final OrderMapper orderMapper;

    private final UserClientService userClientService;

    @Transactional
    @Override
    public ResponseOrderDto createOrder(CreateOrderDto createOrderDto) {
        log.info("Request to create order");
        Order order = orderMapper.toEntity(createOrderDto);

        ResponseUserDto userDto = userClientService.findUserByEmail(createOrderDto.getEmail());
        order.setUserId(userDto.getId());

        List<Item> items = itemRepository.findAllById(createOrderDto.getItems().stream()
                .map(CreateOrderItemDto::getItemId).toList());

        List<OrderItem> orderItems = mapToOrderItems(order, createOrderDto.getItems(), items);

        order.setOrderItemList(orderItems);
        order.setTotalPrice(calculateTotalPrice(orderItems));
        order.setStatus(Status.NEW);
        order.setDeleted(false);

        ResponseOrderDto orderDto = orderMapper.toDto(orderRepository.save(order));
        orderDto.setUser(userDto);

        log.info("Order created successfully with id: {}", orderDto.getId());
        return orderDto;
    }

    @Transactional
    @Override
    public ResponseOrderDto updateById(Long id, Status status) {
        log.info("Request to update order with id: {}, status: {}", id, status);
        Order order = orderRepository.findById(id).orElseThrow(() -> {
            log.error("Order with id {} not found for update", id);
            return new OrderNotFoundException();
        });

        ResponseUserDto userDto = userClientService.findUserById(order.getUserId());
        if (!Status.isValid(status)) {
            log.error("Invalid order status: {} for order id: {}", status, id);
            throw new InvalidOrderStatusException();
        }
        order.setStatus(status);

        ResponseOrderDto orderDto = orderMapper.toDto(orderRepository.save(order));
        orderDto.setUser(userDto);

        log.info("Order with id {} updated successfully", id);
        return orderDto;
    }

    @Override
    public ResponseOrderDto findById(Long id) {
        log.info("Request to find order by id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order with id {} not found", id);
                    return new OrderNotFoundException();
                });

        ResponseOrderDto orderDto = orderMapper.toDto(order);

        ResponseUserDto userDto = userClientService.findUserById(order.getUserId());

        orderDto.setUser(userDto);

        return orderDto;
    }

    @Override
    public List<ResponseOrderDto> findByUserId(Long id) {
        log.info("Request to find orders by user id: {}", id);
        ResponseUserDto userDto = userClientService.findUserById(id);

        List<ResponseOrderDto> orderDtos = orderRepository.findByUserId(userDto.getId()).stream()
                .map(orderMapper::toDto).toList();

        orderDtos.forEach(orderDto -> orderDto.setUser(userDto));

        return orderDtos;
    }

    @Override
    public List<ResponseOrderDto> findBySelfId() {
        log.info("Request to find orders by self id");
        ResponseUserDto userDto = userClientService.findUserBySelfId();

        List<ResponseOrderDto> orderDtos = orderRepository.findByUserId(userDto.getId()).stream()
                .map(orderMapper::toDto).toList();

        orderDtos.forEach(orderDto -> orderDto.setUser(userDto));

        return orderDtos;
    }

    @Override
    public Page<ResponseOrderDto> findAll(Pageable pageable, Instant startDate, Instant endDate, Status status) {
        log.info("Request to find all orders. Page: {}, Size: {}, Status: {}",
                pageable.getPageNumber(), pageable.getPageSize(), status);

        Specification<Order> specification = Specification.where(OrderSpecifications
                .hasDate(startDate, endDate)).and(OrderSpecifications.hasStatus(status));

        Page<Order> orders = orderRepository.findAll(specification, PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize()));

        List<Long> ids = orders.stream()
                .map(Order::getUserId)
                .distinct()
                .toList();

        List<ResponseUserDto> users = userClientService.findAllUsersById(ids);

        Map<Long, ResponseUserDto> ResponseUserDtoMap = users.stream()
                .collect(Collectors.toMap(ResponseUserDto::getId, u -> u));

        return orders.map(order -> {
            ResponseOrderDto orderDto = orderMapper.toDto(order);
            orderDto.setUser(ResponseUserDtoMap.get(order.getUserId()));
            return orderDto;
        });
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        log.info("Request to delete order by id: {}", id);
        if (!orderRepository.existsById(id)) {
            log.error("Order with id {} not found for deletion", id);
            throw new OrderNotFoundException();
        }
        orderRepository.deleteById(id);
        log.info("Order with id {} deleted successfully", id);
    }

    private BigDecimal calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream()
                .map(orderItem ->
                        orderItem.getItem().getPrice()
                                .multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OrderItem> mapToOrderItems(Order order, List<CreateOrderItemDto> dtoItems,
                                            List<Item> items) {
        Map<Long, Item> itemMap = items.stream()
                .collect(Collectors.toMap(Item::getId, i -> i));

        return dtoItems.stream()
                .map(dtoItem -> {
                    Item item = itemMap.get(dtoItem.getItemId());
                    if (item == null) {
                        log.error("Item with id {} not found during mapping to order items", dtoItem.getItemId());
                        throw new ItemNotFoundException();
                    }
                    if(item.getDeleted()) {
                        log.error("Item with id {} is deleted and cannot be added to order", item.getId());
                        throw new DeletedItemException(item.getId());
                    }

                    OrderItem orderItem = new OrderItem();
                    orderItem.setItem(item);
                    orderItem.setQuantity(dtoItem.getQuantity());
                    orderItem.setOrder(order);
                    return orderItem;
                }).toList();
    }

}

