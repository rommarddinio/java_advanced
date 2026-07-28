package com.innowise.userservice.service;

import com.innowise.userservice.dto.user.CreateUserDto;
import com.innowise.userservice.dto.user.ResponseUserDto;
import com.innowise.userservice.dto.user.UpdateUserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    ResponseUserDto createUser(CreateUserDto userDTO);

    ResponseUserDto getUserById(Long id);

    Page<ResponseUserDto> getUsers(Pageable pageable, String name, String surname);

    void activateUser(Long id);

    void deactivateUser(Long id);

    ResponseUserDto updateUser(Long id, UpdateUserDto userDTO);

}
