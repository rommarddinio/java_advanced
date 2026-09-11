package com.innowise.paymentservice.controller;

import com.innowise.paymentservice.details.UserDetailsImpl;
import com.innowise.paymentservice.dto.CreatePaymentDto;
import com.innowise.paymentservice.dto.ResponsePaymentDto;
import com.innowise.paymentservice.dto.TimeRangeDto;
import com.innowise.paymentservice.dto.TotalPaymentAmount;
import com.innowise.paymentservice.enums.Status;
import com.innowise.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public ResponseEntity<ResponsePaymentDto> createPayment( @AuthenticationPrincipal UserDetailsImpl userDetails,
                                                             @RequestBody @Valid CreatePaymentDto createPaymentDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(userDetails.getUserId(), createPaymentDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<ResponsePaymentDto>> getPayments(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Status status,
            @PageableDefault Pageable pageable) {

        return ResponseEntity.ok(paymentService.getPayments(orderId, userId, status, pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/me")
    public ResponseEntity<Page<ResponsePaymentDto>> getSelfPayments(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(paymentService.getPayments(null, userDetails.getUserId(), null, pageable));
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/total-amount/me")
    public ResponseEntity<TotalPaymentAmount> getTotalPaymentSumOfUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody TimeRangeDto rangeDto
    ) {
        return ResponseEntity.ok(paymentService.getTotalSumOfUser(userDetails.getUserId(), rangeDto));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/total-amount/users")
    public ResponseEntity<TotalPaymentAmount> getTotalPaymentSumOfAllUsers(@RequestBody TimeRangeDto rangeDto) {
        return ResponseEntity.ok(paymentService.getTotalSumOfAllUsers(rangeDto));
    }
}
