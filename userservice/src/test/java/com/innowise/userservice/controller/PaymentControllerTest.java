package com.innowise.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.userservice.dto.paymentcard.CreatePaymentCardDto;
import com.innowise.userservice.dto.paymentcard.ResponsePaymentCardDto;
import com.innowise.userservice.dto.paymentcard.UpdatePaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class PaymentControllerTest {

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
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    private PaymentCard paymentCard;

    private CreatePaymentCardDto createPaymentCardDto;

    private UpdatePaymentCardDto updatePaymentCardDto;

    @BeforeEach
    void setUp() {
        paymentCardRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setName("Roman");
        user.setSurname("Sidorchuk");
        user.setBirthDate(LocalDate.of(2006, 2, 28));
        user.setEmail("roman@gmail.com");
        user.setActive(true);
        user = userRepository.save(user);

        paymentCard = new PaymentCard();
        paymentCard.setNumber("1111 1111 1111 1111");
        paymentCard.setHolder("RAMAN SIDARCHUK");
        paymentCard.setExpirationDate(LocalDate.of(2030, 1, 1));
        paymentCard.setActive(true);
        paymentCard.setUser(user);
        paymentCard = paymentCardRepository.save(paymentCard);

        createPaymentCardDto = new CreatePaymentCardDto(user.getId(), "1111 2222 3333 4444",
                "RAMAN SIDARCHUK", LocalDate.of(2030, 1, 1));

        updatePaymentCardDto = new UpdatePaymentCardDto(user.getId(), "0000 2222 3333 4444",
                "PETYA SIDARCHUK", LocalDate.of(2030, 1, 1));
    }

    @Test
    void createPaymentCard_ShouldReturnSavedPaymentCard_WhenSuccessful() throws Exception {
        createPaymentCardDto.setNumber("2222 2222 2222 2222");

        mockMvc.perform(post("/payment-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentCardDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.number").value(createPaymentCardDto.getNumber()))
                .andExpect(jsonPath("$.holder").value(createPaymentCardDto.getHolder()));
    }

    @Test
    void createPaymentCard_ShouldReturn409_WhenCardLimitReached() throws Exception {
        PaymentCard paymentCard2 = new PaymentCard();
        paymentCard2.setNumber("1222 2222 2222 2222");
        paymentCard2.setHolder("RAMAN SIDARCHUK");
        paymentCard2.setExpirationDate(LocalDate.of(2030, 12, 31));
        paymentCard2.setActive(true);
        paymentCard2.setUser(user);
        paymentCardRepository.save(paymentCard2);

        PaymentCard paymentCard3 = new PaymentCard();
        paymentCard3.setNumber("3333 3333 3333 3333");
        paymentCard3.setHolder("RAMAN SIDARCHUK");
        paymentCard3.setExpirationDate(LocalDate.of(2030, 12, 31));
        paymentCard3.setActive(true);
        paymentCard3.setUser(user);
        paymentCardRepository.save(paymentCard3);

        PaymentCard paymentCard4 = new PaymentCard();
        paymentCard4.setNumber("4444 4444 4444 4444");
        paymentCard4.setHolder("RAMAN SIDARCHUK");
        paymentCard4.setExpirationDate(LocalDate.of(2030, 12, 31));
        paymentCard4.setActive(true);
        paymentCard4.setUser(user);
        paymentCardRepository.save(paymentCard4);

        PaymentCard paymentCard5 = new PaymentCard();
        paymentCard5.setNumber("5555 5555 5555 5555");
        paymentCard5.setHolder("RAMAN SIDARCHUK");
        paymentCard5.setExpirationDate(LocalDate.of(2030, 12, 31));
        paymentCard5.setActive(true);
        paymentCard5.setUser(user);
        paymentCardRepository.save(paymentCard5);

        mockMvc.perform(post("/payment-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentCardDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void createPaymentCard_ShouldReturn404_WhenUserNotFound() throws Exception {
        createPaymentCardDto.setUserId(999L);

        mockMvc.perform(post("/payment-cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentCardDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePaymentCard_ShouldUpdatePaymentCard_WhenSuccessful() throws Exception {
        mockMvc.perform(put("/payment-cards/{id}", paymentCard.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePaymentCardDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(updatePaymentCardDto.getNumber()))
                .andExpect(jsonPath("$.holder").value(updatePaymentCardDto.getHolder()));
    }

    @Test
    void updatePaymentCard_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(put("/payment-cards/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePaymentCardDto)))
                .andExpect(status().isNotFound());
    }


    @Test
    void getPaymentCardsById_ShouldReturnPaymentCard_WhenSuccessful() throws Exception {
        mockMvc.perform(get("/payment-cards/{id}", paymentCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(paymentCard.getNumber()))
                .andExpect(jsonPath("$.holder").value(paymentCard.getHolder()));
    }

    @Test
    void getPaymentCardsById_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(get("/payment-cards/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPaymentCardsByUserId_ShouldReturnListOfPaymentCards() throws Exception {
        mockMvc.perform(get("/payment-cards/user/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

    }

    @Test
    void getPaymentCardsByUserId_ShouldReturn404_WhenUserNotFound() throws Exception{
        mockMvc.perform(get("/payment-cards/byUser/99"))
                .andExpect(status().isNotFound());

    }

    @Test
    void getPaymentCards_ShouldReturnPaymentCardPage_WhenSuccessful() throws Exception {
        mockMvc.perform(get("/payment-cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].number").value(paymentCard.getNumber()));

    }

    @Test
    void activatePaymentCard_ShouldChangeActiveStatus_WhenSuccessful() throws Exception {
        mockMvc.perform(patch("/payment-cards/{id}/activate", paymentCard.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void activatePaymentCard_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(patch("/payment-cards/99/activate"))
                .andExpect(status().isNotFound());

    }

    @Test
    void deactivatePaymentCard_ShouldChangeActiveStatus_WhenSuccessful() throws Exception {
        mockMvc.perform(patch("/payment-cards/{id}/deactivate", paymentCard.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivatePaymentCard_ShouldReturn404_WhenNotFound() throws Exception {
        mockMvc.perform(patch("/payment-cards/99/deactivate"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivatePaymentCardsByUserId_WhenSuccessful() throws Exception {
        mockMvc.perform(patch("/payment-cards/deactivate/{id}", user.getId()))
                .andExpect(status().isNoContent());
    }

}
