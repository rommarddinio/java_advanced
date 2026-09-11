package com.innowise.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.dto.item.ResponseItemDto;
import com.innowise.orderservice.dto.item.UpdateItemDto;
import com.innowise.orderservice.dto.item.CreateItemDto;
import com.innowise.orderservice.details.UserDetailsImpl;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "JWT_SECRET=testsecret",
        "USER_SERVICE_URL=service",
        "KAFKA_URI=localhost:9094"
})
class ItemControllerImplTest {

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
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Item testItem;

    private CreateItemDto createDto;

    private UserDetailsImpl admin;

    @BeforeEach
    void setUp() {
        admin = new UserDetailsImpl(2L, "ROLE_ADMIN");
        itemRepository.truncateTable();

        testItem = new Item();
        testItem.setName("Test Item");
        testItem.setPrice(BigDecimal.valueOf(100));
        testItem.setDeleted(false);
        testItem = itemRepository.save(testItem);

        createDto = new CreateItemDto("New Item", BigDecimal.valueOf(200));
    }

    @Test
    void createItem_ShouldReturnCreatedItem_WhenSuccessful() throws Exception {
        mockMvc.perform(post("/items")
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Item"))
                .andExpect(jsonPath("$.price").value(200));
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem_WhenSuccessful() throws Exception {
        UpdateItemDto updateItemDto = new UpdateItemDto();
        updateItemDto.setName("Updated Item");
        updateItemDto.setPrice(BigDecimal.valueOf(150));

        mockMvc.perform(put("/items/{id}", testItem.getId())
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Item"))
                .andExpect(jsonPath("$.price").value(150));
    }

    @Test
    void findById_ShouldReturnItem_WhenSuccessful() throws Exception {
        mockMvc.perform(get("/items/{id}", testItem.getId())
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.price").value(100));
    }

    @Test
    void findAll_ShouldReturnItemPage_WhenSuccessful() throws Exception {
        mockMvc.perform(get("/items")
                        .param("page", "0")
                        .param("size", "10")
                        .with(user(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Test Item"));
    }

    @Test
    void deleteItem_ShouldRemoveItem_WhenSuccessful() throws Exception {
        mockMvc.perform(delete("/items/{id}", testItem.getId())
                        .with(user(admin)))
                .andExpect(status().isOk());

        assertFalse(itemRepository.existsById(testItem.getId()));
    }

    @Test
    void deleteItem_ShouldReturn404_WhenItemNotFound() throws Exception {
        Long nonExistingId = 999L;

        mockMvc.perform(delete("/items/{id}", nonExistingId)
                        .with(user(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItem_ShouldReturn404_WhenItemNotFound() throws Exception {
        UpdateItemDto updateItemDto = new UpdateItemDto();
        updateItemDto.setName("Updated Item");
        updateItemDto.setPrice(BigDecimal.valueOf(150));

        mockMvc.perform(put("/items/{id}", 99L)
                        .with(user(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItemDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_ShouldReturn404_WhenItemNotFound() throws Exception {
        mockMvc.perform(get("/items/{id}", 999L)
                        .with(user(admin)))
                .andExpect(status().isNotFound());
    }
}