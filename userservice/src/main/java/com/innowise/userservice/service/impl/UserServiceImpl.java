package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.user.CreateUserDto;
import com.innowise.userservice.dto.user.ResponseUserDto;
import com.innowise.userservice.dto.user.UpdateUserDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.service.UserService;
import com.innowise.userservice.specification.UserSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final PaymentCardService paymentCardService;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    @Override
    public ResponseUserDto createUser(CreateUserDto userDto) {
        log.info("Creating a new user with email: {}", userDto.getEmail());
        User user = userMapper.toEntity(userDto);

        user.setActive(true);

        ResponseUserDto savedUser = userMapper.toDto(userRepository.save(user));
        log.info("Successfully created user with id = {}", savedUser.getId());
        return savedUser;
    }

    @Cacheable(value = "user", key = "#id")
    @Override
    public ResponseUserDto getUserById(Long id) {
        log.debug("Cache miss: Fetching user from database with id = {}", id);
        return userMapper.toDto(userRepository.findById(id).orElseThrow(() -> {
            log.warn("User not found with id = {}", id);
            return new UserNotFoundException();
        }));
    }

    @Override
    public Page<ResponseUserDto> getUsers(Pageable pageable, String name, String surname) {
        log.info("Fetching page {} of users with filters - name: {}, surname: {}", pageable.getPageNumber(), name, surname);
        Specification<User> specification = Specification.where(UserSpecifications.hasName(name)).
                and(UserSpecifications.hasSurname(surname));

        return userRepository.findAll(specification, PageRequest
                .of(pageable.getPageNumber(), pageable.getPageSize())).map(userMapper::toDto);
    }

    @CacheEvict(value = "user", key = "#id")
    @Transactional
    @Override
    public void activateUser(Long id) {
        log.info("Activating user with id = {}", id);
        userRepository.findById(id).orElseThrow(() -> {
            log.warn("Failed to activate user: User not found with id = {}", id);
            return new UserNotFoundException();
        });

        userRepository.setActive(id, true);
        log.info("User with id = {} successfully activated", id);
    }

    @CacheEvict(value = "user", key = "#id")
    @Transactional
    @Override
    public void deactivateUser(Long id) {
        log.info("Deactivating user with id = {}", id);
        userRepository.findById(id).orElseThrow(() -> {
            log.warn("Failed to deactivate user: User not found with id = {}", id);
            return new UserNotFoundException();
        });

        userRepository.setActive(id, false);

        paymentCardService.deactivatePaymentCardsByUserId(id);
        log.info("User with id = {} and their payment cards successfully deactivated", id);
    }

    @CacheEvict(value = "user", key = "#id")
    @Transactional
    @Override
    public ResponseUserDto updateUser(Long id, UpdateUserDto userDto) {
        log.info("Updating user with id = {}", id);
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.warn("Failed to update user: User not found with id = {}", id);
            return new UserNotFoundException();
        });

        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setSurname(userDto.getSurname());
        user.setBirthDate(userDto.getBirthDate());

        ResponseUserDto updatedUser = userMapper.toDto(userRepository.save(user));
        log.info("User with id = {} successfully updated", id);
        return updatedUser;
    }

    @Cacheable(value = "user", key = "#email")
    @Override
    public ResponseUserDto findByEmail(String email) {
        log.info("Searching user with {} email", email);
        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("User with email {} was not found", email);
            return new UserNotFoundException();
        });

        ResponseUserDto userDto = userMapper.toDto(user);
        log.info("User with email {} was successfully found", email);
        return userDto;
    }

    @Override
    public List<ResponseUserDto> findAllUsersByIds(List<Long> ids) {
        log.info("Finding users by ids");
        return userMapper.toDtoList(userRepository.findAllById(ids));
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with id = {}", userId);
        userRepository.deleteById(userId);
    }
}
