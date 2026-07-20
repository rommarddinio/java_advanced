package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.PaymentCardDTO;
import com.innowise.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface PaymentCardMapper {

    @Mapping(source = "userId", target = "user.id")
    PaymentCard toEntity(PaymentCardDTO paymentCardDTO);

    @Mapping(source = "user.id", target = "userId")
    PaymentCardDTO toDTO(PaymentCard paymentCard);

}
