package com.innowise.orderservice.service;

import com.innowise.orderservice.dto.user.ResponseUserDto;

import java.util.List;

public interface UserClientService {

    ResponseUserDto findUserByEmail(String email);

    ResponseUserDto findUserById(Long id);

    ResponseUserDto findUserBySelfId();

    List<ResponseUserDto> findAllUsersById(List<Long> ids);

}
