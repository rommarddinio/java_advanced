package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.user.CreateUserDto;
import com.innowise.userservice.dto.user.ResponseUserDto;
import com.innowise.userservice.dto.user.UpdateUserDto;
import com.innowise.userservice.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = PaymentCardMapper.class)
public interface UserMapper {

    User toEntity(ResponseUserDto userDto);

    User toEntity(CreateUserDto userDto);

    User toEntity(UpdateUserDto userDto);

    ResponseUserDto toDto(User user);

}
