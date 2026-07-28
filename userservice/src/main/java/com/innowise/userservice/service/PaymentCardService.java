package com.innowise.userservice.service;

import com.innowise.userservice.dto.paymentcard.CreatePaymentCardDto;
import com.innowise.userservice.dto.paymentcard.ResponsePaymentCardDto;
import com.innowise.userservice.dto.paymentcard.UpdatePaymentCardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentCardService {

    ResponsePaymentCardDto createPaymentCard(CreatePaymentCardDto paymentCardDTO);

    ResponsePaymentCardDto getPaymentCardById(Long id);

    List<ResponsePaymentCardDto> getPaymentCardsByUserId(Long userId);

    Page<ResponsePaymentCardDto> getPaymentCards(Pageable pageable, String name, String surname);

    void activatePaymentCard(Long id);

    void deactivatePaymentCard(Long id);

    void deactivatePaymentCardsByUserId(Long userId);

    ResponsePaymentCardDto updatePaymentCard(Long id, UpdatePaymentCardDto paymentCardDTO);

}
