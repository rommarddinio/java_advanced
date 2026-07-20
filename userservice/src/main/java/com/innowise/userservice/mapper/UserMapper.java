package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.UserDTO;
import com.innowise.userservice.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = PaymentCardMapper.class)
public interface UserMapper {

    User toEntity(UserDTO userDTO);

    UserDTO toDTO(User user);

}
