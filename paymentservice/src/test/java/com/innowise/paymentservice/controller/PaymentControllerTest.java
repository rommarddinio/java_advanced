package com.innowise.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.innowise.paymentservice.details.UserDetailsImpl;
import com.innowise.paymentservice.dto.CreatePaymentDto;
import com.innowise.paymentservice.dto.TimeRangeDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.enums.Status;
import com.innowise.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(properties = {
        "JWT_SECRET=rFJ8mYk2YcPz6w0QvV2J0s3vQ8j8XzqV2d1v7cZp8xQ="
})
@AutoConfigureMockMvc
class PaymentControllerTest {

    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");
    static KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

    static {
        mongoDBContainer.start();
        kafkaContainer.start();
    }

    @RegisterExtension
    static WireMockExtension wireMockRule = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String bootstrapServers = kafkaContainer.getBootstrapServers();

        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", () -> bootstrapServers);
        registry.add("spring.kafka.producer.bootstrap-servers", () -> bootstrapServers);

        registry.add("random-number-client.url", wireMockRule::baseUrl);
        registry.add("RANDOM_NUMBER_URL", () -> wireMockRule.baseUrl() + "/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Payment testPayment;
    private CreatePaymentDto createPaymentDto;
    private UserDetailsImpl adminUser;
    private UserDetailsImpl regularUser;

    private final Instant fixedInstant = Instant.now();

    @BeforeEach
    void setUp() {
        adminUser = new UserDetailsImpl(1L, "ROLE_ADMIN");
        regularUser = new UserDetailsImpl(2L, "ROLE_USER");

        paymentRepository.deleteAll();

        testPayment = new Payment();
        testPayment.setId("pay-123");
        testPayment.setOrderId(10L);
        testPayment.setUserId(2L);
        testPayment.setStatus(Status.SUCCESS);
        testPayment.setPaymentAmount(BigDecimal.valueOf(150.0));

        testPayment.setTimestamp(fixedInstant.minusSeconds(60));

        testPayment = paymentRepository.save(testPayment);

        createPaymentDto = new CreatePaymentDto(11L, BigDecimal.valueOf(250.0));
    }

    @Test
    void createPayment_ShouldReturnCreatedPayment_WhenSuccessful() throws Exception {
        wireMockRule.stubFor(WireMock.get(urlEqualTo("/integers/?num=1&min=1&max=100&col=1&base=10&format=plain&rnd=new"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .withBody("4")));

        mockMvc.perform(post("/payments")
                        .with(user(regularUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPaymentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.orderId").value(11))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.paymentAmount").value(250.0));
    }

    @Test
    void getPayments_ShouldReturnPageOfPayments_WhenUserIsAdmin() throws Exception {
        mockMvc.perform(get("/payments")
                        .param("userId", "2")
                        .param("orderId", "10")
                        .param("status", "SUCCESS")
                        .param("page", "0")
                        .param("size", "10")
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value("pay-123"));
    }

    @Test
    void getPayments_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        mockMvc.perform(get("/payments")
                        .with(user(regularUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSelfPayments_ShouldReturnUserPayments_WhenSuccessful() throws Exception {
        mockMvc.perform(get("/payments/me")
                        .param("page", "0")
                        .param("size", "10")
                        .with(user(regularUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].userId").value(2));
    }

    @Test
    void getTotalPaymentSumOfUser_ShouldReturnTotalAmount_WhenSuccessful() throws Exception {
        TimeRangeDto timeRange = new TimeRangeDto(
                fixedInstant.minusSeconds(120),
                fixedInstant
        );

        mockMvc.perform(post("/payments/total-amount/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timeRange))
                        .with(user(regularUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(150.0));
    }

    @Test
    void getTotalPaymentSumOfAllUsers_ShouldReturnTotalAmount_WhenUserIsAdmin() throws Exception {
        TimeRangeDto timeRange = new TimeRangeDto(
                fixedInstant.minusSeconds(120),
                fixedInstant
        );

        mockMvc.perform(post("/payments/total-amount/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timeRange))
                        .with(user(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(150.0));
    }

    @Test
    void getTotalPaymentSumOfAllUsers_ShouldReturnForbidden_WhenUserIsNotAdmin() throws Exception {
        TimeRangeDto timeRange = new TimeRangeDto(
                fixedInstant.minusSeconds(120),
                fixedInstant
        );

        mockMvc.perform(post("/payments/total-amount/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(timeRange))
                        .with(user(regularUser)))
                .andExpect(status().isForbidden());
    }
}
