package com.innowise.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.innowise.orderservice.details.UserDetailsImpl;
import com.innowise.orderservice.dto.order.CreateOrderDto;
import com.innowise.orderservice.dto.orderitem.CreateOrderItemDto;
import com.innowise.orderservice.dto.user.ResponseUserDto;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.entity.OrderItem;
import com.innowise.orderservice.enums.Status;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "JWT_SECRET=testsecret",
        "USER_SERVICE_URL=service"
})
@AutoConfigureMockMvc
@WireMockTest
public class OrderControllerImplTest {

    @RegisterExtension
    static WireMockExtension wireMockExtension = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres")
                    .withDatabaseName("test_db")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("services.user.url", wireMockExtension::baseUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private OrderRepository orderRepository;

    private UserDetailsImpl user;

    private UserDetailsImpl admin;

    private Item item;

    private ResponseUserDto userInfo;

    private CreateOrderDto orderCreateDto;

    private Order order;

    private OrderItem orderItem;

    private String status;

    @BeforeEach
    void setUp() {
        itemRepository.truncateTable();
        orderRepository.truncateTable();

        user = new UserDetailsImpl(1L, "ROLE_USER");
        admin = new UserDetailsImpl(2L, "ROLE_ADMIN");

        item = new Item();
        item.setName("Book");
        item.setPrice(BigDecimal.valueOf(10));
        item.setDeleted(false);
        item = itemRepository.save(item);

        userInfo = new ResponseUserDto(1L, "user", "user", "test@example.com");

        orderCreateDto = new CreateOrderDto("test@example.com",
                List.of(new CreateOrderItemDto(item.getId(), 2L)));

        order = new Order();
        order.setUserId(1L);
        order.setStatus(Status.NEW);
        order.setTotalPrice(BigDecimal.valueOf(20));
        order.setDeleted(false);

        orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setItem(item);
        orderItem.setQuantity(2L);

        order.setOrderItemList(List.of(orderItem));
        order = orderRepository.save(order);

        status = "PAID";
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder_WhenUserExists() throws Exception {
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/search"))
                .withQueryParam("email", equalTo("test@example.com"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(userInfo))));

        mockMvc.perform(post("/orders")
                        .with(user(user))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderCreateDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void createOrder_ShouldReturn404_WhenItemNotFound() throws Exception {
        orderCreateDto.setItems(List.of(new CreateOrderItemDto(99L, 67L)));
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/search"))
                .withQueryParam("email", equalTo("test@example.com"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(userInfo))));

        mockMvc.perform(post("/orders")
                        .with(user(user))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderCreateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrder_ShouldReturn404_WhenItemIsDeleted() throws Exception {
        item.setDeleted(true);
        itemRepository.save(item);
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/search"))
                .withQueryParam("email", equalTo("test@example.com"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(userInfo))));

        mockMvc.perform(post("/orders")
                        .with(user(user))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(orderCreateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateById_ShouldReturnOrderDto_WhenSuccessful() throws Exception {
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/" + order.getUserId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(userInfo))));

        mockMvc.perform(patch("/orders/{id}", order.getId())
                        .with(user(admin))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(status)))
                .andExpect(status().isOk());
    }

    @Test
    void updateById_ShouldReturn404_WhenUserNotFound() throws Exception {
        order.setUserId(99L);
        order = orderRepository.save(order);
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/" + order.getUserId()))
                .willReturn(aResponse()
                        .withStatus(404)));

        mockMvc.perform(patch("/orders/{id}", order.getId())
                        .with(user(admin))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(status)))
                .andExpect(status().isNotFound());

    }

    @Test
    void updateById_ShouldReturn404_WhenOrderNotFound() throws Exception {
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/" + order.getUserId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(userInfo))));

        mockMvc.perform(patch("/orders/{id}", 99L)
                        .with(user(admin))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(status)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateById_ShouldReturn400_WhenStatusIsNotValid() throws Exception {
        status = "BEBRA";
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/" + order.getUserId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(userInfo))));

        mockMvc.perform(patch("/orders/{id}", order.getId())
                        .with(user(admin))
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(status)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_ShouldReturn200_WhenSuccessful() throws Exception {
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/" + order.getUserId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(userInfo))));

        mockMvc.perform(get("/orders/{id}", order.getId())
                        .with(user(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void findById_ShouldReturn403_WhenAccessDenied() throws Exception {
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/" + order.getUserId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(userInfo))));

        mockMvc.perform(get("/orders/{id}", order.getId())
                        .with(user(user)))
                .andExpect(status().isForbidden());
    }

    @Test
    void findById_ShouldReturn404_WhenNotFound() throws Exception {
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/" + order.getUserId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(userInfo))));

        mockMvc.perform(get("/orders/{id}", 99L)
                        .with(user(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void findByUserId_ShouldReturn200_WhenSuccessful() throws Exception {
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/" + order.getUserId()))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(userInfo))));

        mockMvc.perform(get("/orders/users/{userId}", order.getUserId())
                        .with(user(admin)))
                .andExpect(status().isOk());

    }

    @Test
    void findByUserId_ShouldReturn404_WhenUserNotFound() throws Exception {
        order.setUserId(99L);
        order = orderRepository.save(order);
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/" + order.getUserId()))
                .willReturn(aResponse()
                        .withStatus(404)));

        mockMvc.perform(get("/orders/user/{userId}", order.getUserId())
                        .with(user(admin)))
                .andExpect(status().isNotFound());

    }

    @Test
    void findBySelfId_ShouldReturn200_WhenUserFound() throws Exception {
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(userInfo))));

        mockMvc.perform(get("/orders/me")
                        .with(user(user)))
                .andExpect(status().isOk());

        wireMockExtension.verify(getRequestedFor(urlEqualTo("/me")));
    }

    @Test
    void findBySelfId_ShouldReturn404_WhenUserNotFound() throws Exception {
        wireMockExtension.stubFor(WireMock.get(urlPathEqualTo("/me"))
                .willReturn(aResponse()
                        .withStatus(404)));

        mockMvc.perform(get("/orders/me")
                        .with(user(user)))
                .andExpect(status().isNotFound());

        wireMockExtension.verify(getRequestedFor(urlEqualTo("/me")));
    }

    @Test
    void findAll_ShouldReturn200_WithUsers() throws Exception {
        List<ResponseUserDto> users = List.of(userInfo);

        wireMockExtension.stubFor(WireMock.post(urlPathEqualTo("/batch"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsBytes(users))));

        mockMvc.perform(get("/orders")
                        .param("page", "0")
                        .param("size", "10")
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String response = result.getResponse().getContentAsString();
                    Assertions.assertTrue(response.contains("test@example.com"));
                });

        wireMockExtension.verify(postRequestedFor(urlEqualTo("/batch")));
    }

    @Test
    void deleteById_ShouldReturn200_WhenSuccessful() throws Exception {
        mockMvc.perform(delete("/orders/{id}", order.getId())
                .with(user(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteById_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(delete("/orders/{id}", 99L)
                        .with(user(admin)))
                .andExpect(status().isNotFound());
    }
}
