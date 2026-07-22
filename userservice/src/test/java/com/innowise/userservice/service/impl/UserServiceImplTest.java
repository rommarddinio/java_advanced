package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.paymentcard.ResponsePaymentCardDto;
import com.innowise.userservice.dto.user.CreateUserDto;
import com.innowise.userservice.dto.user.ResponseUserDto;
import com.innowise.userservice.dto.user.UpdateUserDto;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.PaymentCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardService paymentCardService;

    @Mock
    private UserMapper userMapper;


    private ResponseUserDto responseUserDto;

    private CreateUserDto createUserDto;

    private UpdateUserDto updateUserDto;

    private User user;

    @BeforeEach
    void setUp() {
        responseUserDto = new ResponseUserDto(1L, "Roman", "Sidorchuk",
                LocalDate.of(2006, 2, 28), "roman@gmail.com", true,
                List.of(new ResponsePaymentCardDto()));

        createUserDto = new CreateUserDto("Roman", "Sidorchuk",
                LocalDate.of(2006, 2, 28), "roman@gmail.com");

        updateUserDto = new UpdateUserDto("Roman", "Sidorchuk",
                LocalDate.of(2006, 2, 28), "romansidorchuk@gmail.com");

        user = new User();
    }

    @Test
    void createUser_ShouldReturnResponseUserDto_WhenSuccessful() {
        user.setEmail(createUserDto.getEmail());

        when(userMapper.toEntity(createUserDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseUserDto);

        ResponseUserDto result = userService.createUser(createUserDto);

        assertNotNull(result);
        assertEquals(responseUserDto.getEmail(), user.getEmail());

        verify(userMapper).toEntity(createUserDto);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailNotUnique() {
        when(userMapper.toEntity(createUserDto)).thenReturn(user);
        when(userRepository.save(user)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(DataIntegrityViolationException.class, () -> userService.createUser(createUserDto));

        verify(userMapper).toEntity(createUserDto);
        verify(userRepository).save(user);
    }

    @Test
    void getUserById_ShouldReturnResponseUserDto_WhenSuccessful() {
        user.setId(1L);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(responseUserDto);

        ResponseUserDto result = userService.getUserById(user.getId());

        assertNotNull(result);
        assertEquals(responseUserDto.getId(), user.getId());

        verify(userRepository).findById(user.getId());
        verify(userMapper).toDto(user);
    }

    @Test
    void getUserById_ShouldThrowUserNotFoundException_WhenNotFound() {
        user.setId(99L);

        when(userRepository.findById(user.getId())).thenThrow(UserNotFoundException.class);

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(user.getId()));

        verify(userRepository).findById(user.getId());
        verifyNoInteractions(userMapper);
    }

    @Test
    void activateUser_ShouldReturnUpdatedRow_WhenSuccessful() {
        userService.activateUser(1L);

        verify(userRepository).setActive(1L, true);
    }

    @Test
    void activateUser_ShouldThrowException_WhenNotFound() {
        doThrow(new UserNotFoundException()).when(userRepository).setActive(99L, true);

        assertThrows(UserNotFoundException.class,
                () -> userService.activateUser(99L));

        verify(userRepository).setActive(99L, true);
    }

    @Test
    void deactivateUser_ShouldReturnUpdatedRow_WhenSuccessful() {
        userService.deactivateUser(1L);

        verify(userRepository).setActive(1L, false);
        verify(paymentCardService).deactivatePaymentCardsByUserId(1L);
    }

    @Test
    void deactivateUser_ShouldThrowException_WhenNotFound() {
        doThrow(new UserNotFoundException()).when(userRepository).setActive(99L, false);

        assertThrows(UserNotFoundException.class,
                () -> userService.deactivateUser(99L));

        verify(userRepository).setActive(99L, false);
    }

    @Test
    void updateUser_ShouldReturnUserDto_WhenSuccessful() {
        user.setId(1L);
        user.setEmail("roman@gmail.com");

        responseUserDto.setEmail(updateUserDto.getEmail());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(responseUserDto);

        ResponseUserDto result = userService.updateUser(1L, updateUserDto);

        assertNotNull(result);
        assertEquals(responseUserDto.getEmail(), result.getEmail());
        assertEquals(responseUserDto.getId(), result.getId());

        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verify(userMapper).toDto(user);
    }

    @Test
    void updateUser_ShouldThrowException_WhenEmailNotUnique() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(DataIntegrityViolationException.class, () -> userService
                .updateUser(1L,updateUserDto));

        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verifyNoInteractions(userMapper);
    }

    @Test
    void updateUser_ShouldThrowException_WhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(99L, updateUserDto));

        verify(userRepository).findById(99L);
        verifyNoInteractions(userMapper);
    }

    @Test
    void getUsers_ShouldReturnPageWithoutFilters() {
        Page<User> userPage = new PageImpl<>(List.of(user),
                PageRequest.of(0, 10), 1);

        when(userRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(userPage);
        when(userMapper.toDto(user)).thenReturn(responseUserDto);

        Page<ResponseUserDto> result = userService
                .getUsers(Pageable.ofSize(10), null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(userRepository).findAll(any(Specification.class), any(PageRequest.class));
        verify(userMapper).toDto(user);
    }

    @Test
    void getUsers_ShouldReturnEmptyPage() {
        Page<User> userPage = new PageImpl<>(List.of(),
                PageRequest.of(0, 10), 0);

        when(userRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(userPage);

        Page<ResponseUserDto> result =
                userService.getUsers(Pageable.ofSize(10), null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void getUsers_ShouldReturnPageWithFilters() {
        user.setName("Roman");
        user.setSurname("Sidorchuk");

        Page<User> userPage = new PageImpl<>(List.of(user),
                PageRequest.of(0, 10), 1);

        when(userRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(userPage);
        when(userMapper.toDto(user)).thenReturn(responseUserDto);

        Page<ResponseUserDto> result = userService
                .getUsers(Pageable.ofSize(10), "Roman", "Sidorchuk");

        assertNotNull(result);
        assertEquals("Roman", result.getContent().getFirst().getName());
        assertEquals("Sidorchuk", result.getContent().getFirst().getSurname());
        assertEquals(1, result.getTotalElements());

        verify(userRepository).findAll(any(Specification.class), any(PageRequest.class));
        verify(userMapper).toDto(user);
    }

}
