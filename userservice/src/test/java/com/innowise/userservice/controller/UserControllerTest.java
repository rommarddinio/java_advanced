package com.innowise.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.details.UserDetailsImpl;
import com.innowise.userservice.dto.paymentcard.ResponsePaymentCardDto;
import com.innowise.userservice.dto.user.CreateUserDto;
import com.innowise.userservice.dto.user.ResponseUserDto;
import com.innowise.userservice.dto.user.UpdateUserDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.repository.UserRepository;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"JWT_SECRET=testsecret", "SERVICE_SECRET=secret"})
public class UserControllerTest {

    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis")
                    .withExposedPorts(6379);

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

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    private ResponseUserDto responseUserDto;

    private CreateUserDto createUserDto;

    private UpdateUserDto updateUserDto;

    private UserDetailsImpl userRole;

    private UserDetailsImpl adminRole;

    @BeforeEach

    void setUp() {
        userRepository.deleteAll();

        user = new User();
        user.setName("Roman");
        user.setSurname("Sidorchuk");
        user.setBirthDate(LocalDate.of(2006, 2, 28));
        user.setEmail("roman@gmail.com");
        user.setActive(true);
        user = userRepository.save(user);

        userRole = new UserDetailsImpl(user.getId(), "ROLE_USER");
        adminRole = new UserDetailsImpl(user.getId(), "ROLE_ADMIN");

        responseUserDto = new ResponseUserDto(1L, "Roman", "Sidorchuk",
                LocalDate.of(2006, 2, 28), "roman@gmail.com", true,
                List.of(new ResponsePaymentCardDto()));

        createUserDto = new CreateUserDto("Roman", "Sidorchuk",
                LocalDate.of(2006, 2, 28), "roman@gmail.com");

        updateUserDto = new UpdateUserDto("Roman", "Sidorchuk",
                LocalDate.of(2006, 2, 28), "romansidorchuk@gmail.com");
    }

    @Test
    void createUser_ShouldReturnSavedPaymentCard_WhenSuccessful() throws Exception {
        createUserDto.setEmail("roma@gmail.com");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Roman"))
                .andExpect(jsonPath("$.email").value("roma@gmail.com"));
    }

    @Test
    void createUser_ShouldReturn400_WhenInvalidData() throws Exception {
        createUserDto.setName(null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_ShouldUpdatePaymentCard_WhenSuccessful() throws Exception {
        mockMvc.perform(put("/users/{id}", user.getId())
                        .with(user(adminRole))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Roman"))
                .andExpect(jsonPath("$.email").value("romansidorchuk@gmail.com"));

    }

    @Test
    void updateUser_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(put("/users/999")
                        .with(user(adminRole))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void findById_ShouldReturnPaymentCard_WhenSuccessful() throws Exception {
        mockMvc.perform(get("/users/{id}", user.getId())
                        .with(user(adminRole)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Roman"))
                .andExpect(jsonPath("$.email").value("roman@gmail.com"));
    }

    @Test
    void findById_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/users/99")
                        .with(user(adminRole)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUsers_ShouldReturnPaymentCardPage_WhenSuccessful() throws Exception {
        mockMvc.perform(get("/users")
                        .with(user(adminRole))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("roman@gmail.com"));
    }

    @Test
    void activateUser_ShouldChangeActiveStatus_WhenSuccessful() throws Exception {
        user.setActive(false);
        userRepository.save(user);

        mockMvc.perform(patch("/users/{id}/activate", user.getId())
                        .with(user(adminRole)))
                .andExpect(status().isNoContent());

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(true, updated.getActive());
    }

    @Test
    void activateUser_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(patch("/users/99/activate")
                        .with(user(adminRole)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivateUser_ShouldChangeActiveStatus_WhenSuccessful() throws Exception {
        user.setActive(true);
        userRepository.save(user);

        mockMvc.perform(patch("/users/{id}/deactivate", user.getId())
                        .with(user(adminRole)))
                .andExpect(status().isNoContent());

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(false, updated.getActive());
    }

    @Test
    void deactivateUser_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(patch("/users/99/deactivate")
                        .with(user(adminRole)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivateUser_ShouldReturn403_WhenAccessDenied() throws Exception {
        mockMvc.perform(patch("/users/1/deactivate")
                .with(user(userRole)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSelfById_ShouldReturnUser_WhenSuccessful() throws Exception {
        mockMvc.perform(get("/users/me")
                .with(user(userRole)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Roman"))
                .andExpect(jsonPath("$.email").value("roman@gmail.com"));
    }

}
