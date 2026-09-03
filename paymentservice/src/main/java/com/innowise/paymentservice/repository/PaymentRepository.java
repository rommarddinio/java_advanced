package com.innowise.paymentservice.repository;

import com.innowise.paymentservice.dto.TotalPaymentAmount;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.enums.Status;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, Long> {

    @Aggregation(pipeline = {
            "{ $match: { 'userId': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } } }",
            "{ $group: { '_id': null, 'total': { $sum: '$paymentAmount' } } }"
    })
    TotalPaymentAmount getTotalSumOfUser(Long userId, Instant from, Instant to);

    @Aggregation(pipeline = {
            "{ $match: { 'status': 'SUCCESS', 'timestamp': { $gte: ?0, $lte: ?1 } } }",
            "{ $group: { '_id': null, 'total': { $sum: '$paymentAmount' } } }"
    })
    TotalPaymentAmount getTotalSumOfAllUsers(Instant from, Instant to);

    Boolean existsByOrderIdAndStatus(Long orderId, Status status);

    boolean existsByUserId(Long userId);
}
