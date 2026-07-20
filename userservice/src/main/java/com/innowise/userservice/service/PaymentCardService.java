package com.innowise.userservice.service;

import com.innowise.userservice.dto.PaymentCardDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentCardService {

    PaymentCardDTO createPaymentCard(PaymentCardDTO paymentCardDTO);

    PaymentCardDTO getPaymentCardById(Long id);

    List<PaymentCardDTO> getPaymentCardsByUserId(Long userId);

    Page<PaymentCardDTO> getPaymentCards(Pageable pageable, String name, String surname);

    void activatePaymentCard();

    void deactivatePaymentCard();

    void deactivatePaymentCardsByUserId();

    PaymentCardDTO updatePaymentCard(PaymentCardDTO paymentCardDTO);

}
