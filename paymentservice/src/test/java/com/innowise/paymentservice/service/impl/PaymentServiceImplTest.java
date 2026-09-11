package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.dto.CreatePaymentDto;
import com.innowise.paymentservice.dto.CreatePaymentEvent;
import com.innowise.paymentservice.dto.ResponsePaymentDto;
import com.innowise.paymentservice.dto.TimeRangeDto;
import com.innowise.paymentservice.dto.TotalPaymentAmount;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.enums.Status;
import com.innowise.paymentservice.exception.PaymentNotFoundException;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private RandomNumberClient randomNumberClient;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private CreatePaymentDto createPaymentDto;
    private Payment payment;
    private ResponsePaymentDto responsePaymentDto;
    private TimeRangeDto timeRangeDto;
    private TotalPaymentAmount totalPaymentAmount;

    @BeforeEach
    void setUp() {
        createPaymentDto = new CreatePaymentDto(1L,  BigDecimal.valueOf(500));

        payment = new Payment();
        payment.setId("pay-123");
        payment.setOrderId(1L);
        payment.setUserId(100L);
        payment.setPaymentAmount(BigDecimal.valueOf(500));

        responsePaymentDto = new ResponsePaymentDto("pay-123", 100L, 1L, Status.SUCCESS, Instant.now(), BigDecimal.valueOf(500));
        timeRangeDto = new TimeRangeDto(Instant.now().minusSeconds(3600), Instant.now());
        totalPaymentAmount = new TotalPaymentAmount(BigDecimal.valueOf(500));
    }

    @Test
    void createPayment_ShouldReturnSuccessPayment_WhenRandomNumberIsEven() {
        Long userId = 100L;
        CreatePaymentEvent createPaymentEvent = new CreatePaymentEvent(1L, "SUCCESS");

        when(paymentRepository.existsByOrderIdAndStatus(1L, Status.SUCCESS)).thenReturn(false);
        when(paymentMapper.toEntity(createPaymentDto)).thenReturn(payment);
        when(randomNumberClient.getNumber()).thenReturn(" 4 ");
        when(paymentMapper.toCreatePaymentEvent(payment)).thenReturn(createPaymentEvent);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(responsePaymentDto);

        ResponsePaymentDto result = paymentService.createPayment(userId, createPaymentDto);

        assertNotNull(result);
        assertEquals(Status.SUCCESS, payment.getStatus());
        assertEquals(userId, payment.getUserId());
        assertEquals(responsePaymentDto, result);

        verify(paymentRepository).existsByOrderIdAndStatus(1L, Status.SUCCESS);
        verify(paymentMapper).toEntity(createPaymentDto);
        verify(randomNumberClient).getNumber();
        verify(kafkaProducerService).sendToKafka(createPaymentEvent);
        verify(paymentRepository).save(payment);
        verify(paymentMapper).toDto(payment);
    }

    @Test
    void createPayment_ShouldReturnFailedPayment_WhenRandomNumberIsOdd() {
        Long userId = 100L;
        CreatePaymentEvent createPaymentEvent = new CreatePaymentEvent(1L, "FAILED");
        responsePaymentDto = new ResponsePaymentDto("pay-123", 100L, 1L, Status.FAILED, Instant.now(), BigDecimal.valueOf(500));

        when(paymentRepository.existsByOrderIdAndStatus(1L, Status.SUCCESS)).thenReturn(false);
        when(paymentMapper.toEntity(createPaymentDto)).thenReturn(payment);
        when(randomNumberClient.getNumber()).thenReturn("3");
        when(paymentMapper.toCreatePaymentEvent(payment)).thenReturn(createPaymentEvent);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(responsePaymentDto);

        ResponsePaymentDto result = paymentService.createPayment(userId, createPaymentDto);

        assertNotNull(result);
        assertEquals(Status.FAILED, payment.getStatus());
        assertEquals(userId, payment.getUserId());
        assertEquals(Status.FAILED, result.status());

        verify(paymentRepository).existsByOrderIdAndStatus(1L, Status.SUCCESS);
        verify(paymentMapper).toEntity(createPaymentDto);
        verify(randomNumberClient).getNumber();
        verify(kafkaProducerService).sendToKafka(createPaymentEvent);
        verify(paymentRepository).save(payment);
        verify(paymentMapper).toDto(payment);
    }


    @Test
    void createPayment_ShouldThrowIllegalStateException_WhenOrderAlreadyPaid() {
        Long userId = 100L;
        when(paymentRepository.existsByOrderIdAndStatus(1L, Status.SUCCESS)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> paymentService.createPayment(userId, createPaymentDto));

        verify(paymentRepository).existsByOrderIdAndStatus(1L, Status.SUCCESS);
        verifyNoInteractions(paymentMapper, randomNumberClient, kafkaProducerService);
    }


    @Test
    void getTotalSumOfUser_ShouldReturnAmount_WhenUserHasPayments() {
        when(paymentRepository.existsByUserId(100L)).thenReturn(true);
        when(paymentRepository.getTotalSumOfUser(100L, timeRangeDto.from(), timeRangeDto.to())).thenReturn(totalPaymentAmount);

        TotalPaymentAmount result = paymentService.getTotalSumOfUser(100L, timeRangeDto);

        assertNotNull(result);
        assertEquals(totalPaymentAmount.total(), result.total());

        verify(paymentRepository).existsByUserId(100L);
        verify(paymentRepository).getTotalSumOfUser(100L, timeRangeDto.from(), timeRangeDto.to());
    }

    @Test
    void getTotalSumOfUser_ShouldThrowPaymentNotFoundException_WhenUserHasNoPayments() {
        when(paymentRepository.existsByUserId(999L)).thenReturn(false);

        assertThrows(PaymentNotFoundException.class, () -> paymentService.getTotalSumOfUser(999L, timeRangeDto));

        verify(paymentRepository).existsByUserId(999L);
        verify(paymentRepository, never()).getTotalSumOfUser(anyLong(), any(), any());
    }

    @Test
    void getTotalSumOfAllUsers_ShouldReturnAmount() {
        when(paymentRepository.getTotalSumOfAllUsers(timeRangeDto.from(), timeRangeDto.to())).thenReturn(totalPaymentAmount);

        TotalPaymentAmount result = paymentService.getTotalSumOfAllUsers(timeRangeDto);

        assertNotNull(result);
        assertEquals(totalPaymentAmount.total(), result.total());

        verify(paymentRepository).getTotalSumOfAllUsers(timeRangeDto.from(), timeRangeDto.to());
    }

    @Test
    void getPayments_ShouldReturnPageOfPayments_WhenCriteriaProvided() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = Collections.singletonList(payment);
        List<ResponsePaymentDto> dtoList = Collections.singletonList(responsePaymentDto);

        when(mongoTemplate.count(any(Query.class), eq(Payment.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Payment.class))).thenReturn(payments);
        when(paymentMapper.toDtoList(payments)).thenReturn(dtoList);

        Page<ResponsePaymentDto> result = paymentService.getPayments(1L, 100L, Status.SUCCESS, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(responsePaymentDto, result.getContent().get(0));

        verify(mongoTemplate, atLeastOnce()).count(any(Query.class), eq(Payment.class));
        verify(mongoTemplate, atLeastOnce()).find(any(Query.class), eq(Payment.class));
        verify(paymentMapper).toDtoList(payments);
    }

    @Test
    void getPayments_ShouldReturnPage_WhenAllCriteriaAreNull() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = Collections.singletonList(payment);
        List<ResponsePaymentDto> dtoList = Collections.singletonList(responsePaymentDto);

        when(mongoTemplate.count(any(Query.class), eq(Payment.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Payment.class))).thenReturn(payments);
        when(paymentMapper.toDtoList(payments)).thenReturn(dtoList);

        Page<ResponsePaymentDto> result = paymentService.getPayments(null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(mongoTemplate).count(any(Query.class), eq(Payment.class));
        verify(mongoTemplate).find(any(Query.class), eq(Payment.class));
        verify(paymentMapper).toDtoList(payments);
    }

    @Test
    void getPayments_ShouldReturnPage_WhenUserIdIsPresent() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = Collections.singletonList(payment);
        List<ResponsePaymentDto> dtoList = Collections.singletonList(responsePaymentDto);

        when(mongoTemplate.count(any(Query.class), eq(Payment.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Payment.class))).thenReturn(payments);
        when(paymentMapper.toDtoList(payments)).thenReturn(dtoList);

        Page<ResponsePaymentDto> result = paymentService.getPayments(null, 100L, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(mongoTemplate).count(any(Query.class), eq(Payment.class));
        verify(mongoTemplate).find(any(Query.class), eq(Payment.class));
        verify(paymentMapper).toDtoList(payments);
    }

    @Test
    void getPayments_ShouldReturnPage_WhenOrderIdIsPresent() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = Collections.singletonList(payment);
        List<ResponsePaymentDto> dtoList = Collections.singletonList(responsePaymentDto);

        when(mongoTemplate.count(any(Query.class), eq(Payment.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Payment.class))).thenReturn(payments);
        when(paymentMapper.toDtoList(payments)).thenReturn(dtoList);

        Page<ResponsePaymentDto> result = paymentService.getPayments(1L, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(mongoTemplate).count(any(Query.class), eq(Payment.class));
        verify(mongoTemplate).find(any(Query.class), eq(Payment.class));
        verify(paymentMapper).toDtoList(payments);
    }

    @Test
    void getPayments_ShouldReturnPage_WhenStatusIsPresent() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = Collections.singletonList(payment);
        List<ResponsePaymentDto> dtoList = Collections.singletonList(responsePaymentDto);

        when(mongoTemplate.count(any(Query.class), eq(Payment.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Payment.class))).thenReturn(payments);
        when(paymentMapper.toDtoList(payments)).thenReturn(dtoList);

        Page<ResponsePaymentDto> result = paymentService.getPayments(null, null, Status.SUCCESS, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(mongoTemplate).count(any(Query.class), eq(Payment.class));
        verify(mongoTemplate).find(any(Query.class), eq(Payment.class));
        verify(paymentMapper).toDtoList(payments);
    }
}
