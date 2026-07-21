package com.innowise.userservice.controller;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment-cards")
public class PaymentCardController {

    private final PaymentCardService paymentCardService;

    @GetMapping
    public ResponseEntity<Page<ResponsePaymentCardDto>> getPaymentCards(@PageableDefault Pageable pageable,
                                                                        @RequestParam(required = false) String name,
                                                                        @RequestParam(required = false) String surname) {
        return ResponseEntity.ok(paymentCardService.getPaymentCards(pageable, name, surname));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponsePaymentCardDto> getPaymentCardById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.getPaymentCardById(id));
    }

    @PostMapping
    public ResponseEntity<ResponsePaymentCardDto> createPaymentCard(@RequestBody @Valid CreatePaymentCardDto paymentCardDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentCardService.createPaymentCard(paymentCardDto));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<ResponsePaymentCardDto>> getPaymentCardsByUserId(@PathVariable Long id) {
        return ResponseEntity.ok(paymentCardService.getPaymentCardsByUserId(id));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activatePaymentCard(@PathVariable Long id) {
        paymentCardService.activatePaymentCard(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePaymentCard(@PathVariable Long id) {
        paymentCardService.deactivatePaymentCard(id);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/deactivate/{userId}")
    public ResponseEntity<Void> deactivatePaymentCardsByUserId(@PathVariable Long userId) {
        paymentCardService.deactivatePaymentCardsByUserId(userId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponsePaymentCardDto> updatePaymentCard(@PathVariable Long id,
                                                                    @RequestBody @Valid UpdatePaymentCardDto paymentCardDto) {
        return ResponseEntity.ok(paymentCardService.updatePaymentCard(id, paymentCardDto));
    }

}
