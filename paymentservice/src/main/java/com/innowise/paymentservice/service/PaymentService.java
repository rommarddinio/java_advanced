package com.innowise.paymentservice.service;

import com.innowise.paymentservice.dto.CreatePaymentDto;
import com.innowise.paymentservice.dto.ResponsePaymentDto;
import com.innowise.paymentservice.dto.TimeRangeDto;
import com.innowise.paymentservice.dto.TotalPaymentAmount;
import com.innowise.paymentservice.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    ResponsePaymentDto createPayment(Long id, CreatePaymentDto createPaymentDto);

    TotalPaymentAmount getTotalSumOfUser(Long userId, TimeRangeDto rangeDto);

    TotalPaymentAmount getTotalSumOfAllUsers(TimeRangeDto rangeDto);

    Page<ResponsePaymentDto> getPayments(Long orderId, Long userId, Status status, Pageable pageable);

}
