package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.paymentcard.CreatePaymentCardDto;
import com.innowise.userservice.dto.paymentcard.ResponsePaymentCardDto;
import com.innowise.userservice.dto.paymentcard.UpdatePaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface PaymentCardMapper {

    @Mapping(source = "userId", target = "user.id")
    PaymentCard toEntity(CreatePaymentCardDto paymentCardDto);

    @Mapping(source = "userId", target = "user.id")
    PaymentCard toEntity(UpdatePaymentCardDto paymentCardDto);

    @Mapping(source = "userId", target = "user.id")
    PaymentCard toEntity(ResponsePaymentCardDto paymentCardDto);

    @Mapping(source = "user.id", target = "userId")
    ResponsePaymentCardDto toDto(PaymentCard paymentCard);

    List<ResponsePaymentCardDto> toDtoList(List<PaymentCard> paymentCards);

}
