package com.innowise.userservice.controller;

import com.innowise.userservice.details.UserDetailsImpl;
import com.innowise.userservice.dto.paymentcard.CreatePaymentCardDto;
import com.innowise.userservice.dto.paymentcard.ResponsePaymentCardDto;
import com.innowise.userservice.dto.paymentcard.UpdatePaymentCardDto;
import com.innowise.userservice.service.PaymentCardService;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment-cards")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<ResponsePaymentCardDto>> getPaymentCards(@PageableDefault Pageable pageable,
                                                                        @RequestParam(required = false) String name,
                                                                        @RequestParam(required = false) String surname) {
        return ResponseEntity.ok(paymentCardService.getPaymentCards(pageable, name, surname));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponsePaymentCardDto> getPaymentCardById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.getPaymentCardById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ResponsePaymentCardDto> createPaymentCard(@RequestBody @Valid CreatePaymentCardDto paymentCardDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardService.createPaymentCard(paymentCardDto));
    }

    @PostMapping("/me")
    public ResponseEntity<ResponsePaymentCardDto> createSelfPaymentCard(@RequestBody @Valid CreatePaymentCardDto paymentCardDto,
                                                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        paymentCardDto.setUserId(userDetails.getUserId());
        return ResponseEntity.ok(paymentCardService.createPaymentCard(paymentCardDto));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{id}")
    public ResponseEntity<List<ResponsePaymentCardDto>> getPaymentCardsByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.getPaymentCardsByUserId(id));
    }

    @GetMapping("/user/me")
    public ResponseEntity<List<ResponsePaymentCardDto>> getPaymentCardsBySelfId(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(paymentCardService.getPaymentCardsByUserId(userDetails.getUserId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activatePaymentCard(@PathVariable Long id) {
        paymentCardService.activatePaymentCard(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePaymentCard(@PathVariable Long id) {
        paymentCardService.deactivatePaymentCard(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/deactivate/{userId}")
    public ResponseEntity<Void> deactivatePaymentCardsByUserId(@PathVariable Long userId) {
        paymentCardService.deactivatePaymentCardsByUserId(userId);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponsePaymentCardDto> updatePaymentCard(@PathVariable Long id,
                                                                    @RequestBody @Valid UpdatePaymentCardDto paymentCardDto) {
        return ResponseEntity.ok(paymentCardService.updatePaymentCard(id, paymentCardDto));
    }

}
