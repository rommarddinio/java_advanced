package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.dto.CreatePaymentDto;
import com.innowise.paymentservice.dto.ResponsePaymentDto;
import com.innowise.paymentservice.dto.CreatePaymentEvent;
import com.innowise.paymentservice.entity.Payment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    Payment toEntity(CreatePaymentDto paymentDto);

    ResponsePaymentDto toDto(Payment payment);

    CreatePaymentEvent toCreatePaymentEvent(Payment payment);

    List<ResponsePaymentDto> toDtoList(List<Payment> payments);

}
