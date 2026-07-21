package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.user.CreateUserDto;
import com.innowise.userservice.dto.user.ResponseUserDto;
import com.innowise.userservice.dto.user.UpdateUserDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specification.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private PaymentCardRepository paymentCardRepository;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    public ResponseUserDto createUser(CreateUserDto userDto) {
        User user = userMapper.toEntity(userDto);
        
        user.setActive(true);

        return userMapper.toDto(userRepository.save(user));
    }


    @Override
    public ResponseUserDto getUserById(Long id) {
        return userMapper.toDto(userRepository.findById(id).orElseThrow(UserNotFoundException::new));
    }

    @Override
    public Page<ResponseUserDto> getUsers(Pageable pageable, String name, String surname) {
        Specification<User> specification = Specification.where(UserSpecifications.hasName(name)).
                and(UserSpecifications.hasSurname(surname));

        return userRepository.findAll(specification, PageRequest
                .of(pageable.getPageNumber(), pageable.getPageSize())).map(userMapper::toDto);
    }

    @Transactional
    @Override
    public void activateUser(Long id) {
        userRepository.setActive(id, true);
    }

    @Transactional
    @Override
    public void deactivateUser(Long id) {
        userRepository.setActive(id, false);

        paymentCardRepository.deactivateByUserId(id);
    }

    @Transactional
    @Override
    public ResponseUserDto updateUser(Long id, UpdateUserDto userDto) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setSurname(userDto.getSurname());
        user.setBirthDate(userDto.getBirthDate());

        return userMapper.toDto(userRepository.save(user));
    }
}
