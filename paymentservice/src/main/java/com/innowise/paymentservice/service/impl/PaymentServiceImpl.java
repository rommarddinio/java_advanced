package com.innowise.paymentservice.service.impl;

import com.innowise.paymentservice.client.RandomNumberClient;
import com.innowise.paymentservice.dto.CreatePaymentDto;
import com.innowise.paymentservice.dto.ResponsePaymentDto;
import com.innowise.paymentservice.dto.TimeRangeDto;
import com.innowise.paymentservice.dto.TotalPaymentAmount;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.enums.Status;
import com.innowise.paymentservice.exception.PaymentNotFoundException;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.KafkaProducerService;
import com.innowise.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    private final PaymentMapper paymentMapper;

    private final RandomNumberClient randomNumberClient;

    private final MongoTemplate mongoTemplate;

    private final KafkaProducerService kafkaProducerService;

    @Override
    public ResponsePaymentDto createPayment(Long id, CreatePaymentDto createPaymentDto) {
        log.info("Attempting to create payment for user id: {} and order id: {}", id, createPaymentDto.orderId());

        if(paymentRepository.existsByOrderIdAndStatus(createPaymentDto.orderId(), Status.SUCCESS)) {
            log.warn("Payment failed: Order id {} is already paid", createPaymentDto.orderId());
            throw new IllegalStateException("Order is already paid");
        }

        Payment payment = paymentMapper.toEntity(createPaymentDto);

        log.debug("Fetching random number for payment logic...");
        int number = Integer.parseInt(randomNumberClient.getNumber().trim());
        Status status = number % 2 == 0 ? Status.SUCCESS : Status.FAILED;
        log.debug("Random number received: {}, calculated payment status: {}", number, status);

        payment.setStatus(status);
        payment.setUserId(id);

        log.info("Sending payment status event to Kafka for order id: {}", payment.getOrderId());
        kafkaProducerService.sendToKafka(paymentMapper.toCreatePaymentEvent(payment));

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment successfully processed and saved with status: {} for order id: {}", status, savedPayment.getOrderId());

        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public TotalPaymentAmount getTotalSumOfUser(Long userId, TimeRangeDto rangeDto) {
        log.info("Requesting total payment sum for user id: {} in range from {} to {}", userId, rangeDto.from(), rangeDto.to());

        if (!paymentRepository.existsByUserId(userId)) {
            log.warn("Total sum request failed: User id {} has no payments", userId);
            throw new PaymentNotFoundException("User has no payments yet");
        }

        TotalPaymentAmount result = paymentRepository.getTotalSumOfUser(userId, rangeDto.from(), rangeDto.to());

        TotalPaymentAmount finalResult = (result != null && result.total() != null)
                ? result
                : new TotalPaymentAmount(java.math.BigDecimal.ZERO);

        log.info("Total payment sum calculated for user id: {}. Total amount: {}", userId, finalResult.total());
        return finalResult;
    }

    @Override
    public TotalPaymentAmount getTotalSumOfAllUsers(TimeRangeDto rangeDto) {
        log.info("Requesting total payment sum for all users in range from {} to {}", rangeDto.from(), rangeDto.to());

        TotalPaymentAmount result = paymentRepository.getTotalSumOfAllUsers(rangeDto.from(), rangeDto.to());

        TotalPaymentAmount finalResult = (result != null && result.total() != null)
                ? result
                : new TotalPaymentAmount(java.math.BigDecimal.ZERO);

        log.info("Total payment sum calculated for all users. Total amount: {}", finalResult.total());
        return finalResult;
    }

    @Override
    public Page<ResponsePaymentDto> getPayments(Long orderId, Long userId, Status status, Pageable pageable) {
        log.info("Searching payments with filters - OrderId: {}, UserId: {}, Status: {}, Pageable: {}",
                orderId, userId, status, pageable);

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (userId != null) {
            criteriaList.add(Criteria.where("userId").is(userId));
        }
        if (orderId != null) {
            criteriaList.add(Criteria.where("orderId").is(orderId));
        }
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long totalCount = mongoTemplate.count(query, Payment.class);
        log.debug("Total matching records found in Mongo: {}", totalCount);

        query.with(pageable);

        List<Payment> payments = mongoTemplate.find(query, Payment.class);
        log.info("Successfully fetched {} payment records for current page", payments.size());

        return new PageImpl<>(paymentMapper.toDtoList(payments), pageable, totalCount);
    }

}
