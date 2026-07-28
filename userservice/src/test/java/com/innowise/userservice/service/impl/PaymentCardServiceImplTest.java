package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.paymentcard.CreatePaymentCardDto;
import com.innowise.userservice.dto.paymentcard.ResponsePaymentCardDto;
import com.innowise.userservice.dto.paymentcard.UpdatePaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.CardLimitException;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PaymentCardServiceImplTest {

    @InjectMocks
    private PaymentCardServiceImpl paymentCardService;

    @Mock
    private PaymentCardRepository paymentCardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper paymentCardMapper;

    @Mock
    private CacheManager cacheManager;

    private ResponsePaymentCardDto responsePaymentCardDto;

    private CreatePaymentCardDto createPaymentCardDto;

    private UpdatePaymentCardDto updatePaymentCardDto;

    private PaymentCard paymentCard;

    private User user;

    private Cache cache;

    @BeforeEach
    void setUp() {
        responsePaymentCardDto = new ResponsePaymentCardDto(1L, 1L, "1111 2222 3333 4444",
                "RAMAN SIDARCHUK", LocalDate.of(2030, 1, 1), true);

        createPaymentCardDto = new CreatePaymentCardDto(1L, "1111 2222 3333 4444",
                "RAMAN SIDARCHUK", LocalDate.of(2030, 1, 1));

        updatePaymentCardDto = new UpdatePaymentCardDto(1L, "0000 2222 3333 4444",
                "PETYA SIDARCHUK", LocalDate.of(2030, 1, 1));

        paymentCard = new PaymentCard();

        user = new User();

        cache = Mockito.mock(Cache.class);
    }

    @Test
    void createPaymentCard_ShouldReturnPaymentCardDto_WhenSuccessful() {
        user.setId(1L);
        paymentCard.setUser(user);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(user.getId())).thenReturn(1);
        when(paymentCardMapper.toEntity(createPaymentCardDto)).thenReturn(paymentCard);
        when(paymentCardRepository.save(paymentCard)).thenReturn(paymentCard);
        when(paymentCardMapper.toDto(paymentCard)).thenReturn(responsePaymentCardDto);

        ResponsePaymentCardDto result = paymentCardService.createPaymentCard(createPaymentCardDto);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());

        verify(userRepository).findById(1L);
        verify(paymentCardRepository).countByUserId(1L);
        verify(paymentCardMapper).toEntity(createPaymentCardDto);
        verify(paymentCardRepository).save(paymentCard);
        verify(paymentCardMapper).toDto(paymentCard);
    }

    @Test
    void createPaymentCard_ShouldThrowException_WhenUserNotFound() {
        createPaymentCardDto.setUserId(99L);

        when(userRepository.findById(createPaymentCardDto.getUserId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                paymentCardService.createPaymentCard(createPaymentCardDto));

        verify(userRepository).findById(createPaymentCardDto.getUserId());
        verifyNoInteractions(paymentCardMapper);
        verifyNoInteractions(paymentCardRepository);
    }

    @Test
    void createPaymentCard_ShouldThrowException_WhenUserReachedLimitOfCards() {
        createPaymentCardDto.setUserId(1L);
        user.setId(1L);

        Set<PaymentCard> paymentCardSet = Set.of(new PaymentCard(), new PaymentCard(),
                new PaymentCard(), new PaymentCard(), new PaymentCard());
        user.setPaymentCards(paymentCardSet);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(user.getId())).thenReturn(5);

        assertThrows(CardLimitException.class, () ->
                paymentCardService.createPaymentCard(createPaymentCardDto));

        verify(userRepository).findById(1L);
        verify(paymentCardRepository).countByUserId(1L);
        verifyNoInteractions(paymentCardMapper);
    }

    @Test
    void createPaymentCard_ShouldThrowException_WhenNumberNotUnique() {
        createPaymentCardDto.setUserId(1L);
        user.setId(1L);
        paymentCard.setUser(user);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(paymentCardRepository.countByUserId(user.getId())).thenReturn(1);
        when(paymentCardMapper.toEntity(createPaymentCardDto)).thenReturn(paymentCard);
        when(paymentCardRepository.save(paymentCard)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(DataIntegrityViolationException.class, () ->
                paymentCardService.createPaymentCard(createPaymentCardDto));

        verify(userRepository).findById(1L);
        verify(paymentCardRepository).countByUserId(1L);
        verify(paymentCardMapper).toEntity(createPaymentCardDto);
        verify(paymentCardRepository).save(paymentCard);
    }

    @Test
    void updatePaymentCard_ShouldReturnUpdatedPaymentCardDto_WhenSuccessful() {
        paymentCard.setId(1L);
        user.setId(10L);
        updatePaymentCardDto.setUserId(10L);
        responsePaymentCardDto.setUserId(10L);

        when(paymentCardRepository.findById(paymentCard.getId())).thenReturn(Optional.of(paymentCard));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(paymentCardRepository.save(paymentCard)).thenReturn(paymentCard);
        when(paymentCardMapper.toDto(paymentCard)).thenReturn(responsePaymentCardDto);

        ResponsePaymentCardDto result = paymentCardService.updatePaymentCard(paymentCard.getId(),
                updatePaymentCardDto);

        assertNotNull(result);
        assertEquals(responsePaymentCardDto.getNumber(), result.getNumber());
        assertEquals(updatePaymentCardDto.getUserId(), result.getUserId());

        verify(paymentCardRepository).findById(paymentCard.getId());
        verify(userRepository).findById(user.getId());
        verify(paymentCardRepository).save(paymentCard);
        verify(paymentCardMapper).toDto(paymentCard);
    }

    @Test
    void updatePaymentCard_ShouldThrowException_WhenNotFound() {
        paymentCard.setId(99L);
        when(paymentCardRepository.findById(paymentCard.getId())).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                paymentCardService.updatePaymentCard(paymentCard.getId(), updatePaymentCardDto));

        verify(paymentCardRepository).findById(paymentCard.getId());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(paymentCardMapper);
    }

    @Test
    void updatePaymentCard_ShouldThrowException_WhenUserNotFound() {
        paymentCard.setId(1L);
        updatePaymentCardDto.setUserId(10L);

        when(paymentCardRepository.findById(paymentCard.getId())).thenReturn(Optional.of(paymentCard));
        when(userRepository.findById(updatePaymentCardDto.getUserId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                paymentCardService.updatePaymentCard(1L, updatePaymentCardDto));

        verify(paymentCardRepository).findById(paymentCard.getId());
        verify(userRepository).findById(updatePaymentCardDto.getUserId());
        verifyNoInteractions(paymentCardMapper);
    }

    @Test
    void updatePaymentCard_ShouldThrowException_WhenNumberNotUnique() {
        paymentCard.setId(1L);
        updatePaymentCardDto.setUserId(10L);

        when(paymentCardRepository.findById(paymentCard.getId())).thenReturn(Optional.of(paymentCard));
        when(userRepository.findById(updatePaymentCardDto.getUserId())).thenReturn(Optional.of(user));
        when(paymentCardRepository.save(paymentCard)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(DataIntegrityViolationException.class, () ->
                paymentCardService.updatePaymentCard(paymentCard.getId(), updatePaymentCardDto));

        verify(paymentCardRepository).findById(paymentCard.getId());
        verify(userRepository).findById(updatePaymentCardDto.getUserId());
        verify(paymentCardRepository).save(paymentCard);
        verifyNoInteractions(paymentCardMapper);
    }

    @Test
    void updateSelfPaymentCard_ShouldThrowException_WhenNotFound() {
        paymentCard.setId(99L);
        when(paymentCardRepository.findById(paymentCard.getId())).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                paymentCardService.updatePaymentCard(paymentCard.getId(), updatePaymentCardDto));

        verify(paymentCardRepository).findById(paymentCard.getId());
        verifyNoInteractions(userRepository);
        verifyNoInteractions(paymentCardMapper);
    }


    @Test
    void getPaymentCards_ShouldReturnPageWithoutFilters() {
        Page<PaymentCard> paymentCardPage = new PageImpl<>(List.of(paymentCard),
                PageRequest.of(0, 10), 1);

        when(paymentCardRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(paymentCardPage);
        when(paymentCardMapper.toDto(paymentCard)).thenReturn(responsePaymentCardDto);

        Page<ResponsePaymentCardDto> result = paymentCardService
                .getPaymentCards(Pageable.ofSize(10), null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(paymentCardRepository).findAll(any(Specification.class), any(PageRequest.class));
        verify(paymentCardMapper).toDto(paymentCard);
    }

    @Test
    void getPaymentCards_ShouldReturnEmptyPage() {
        Page<PaymentCard> paymentCardPage = new PageImpl<>(List.of(),
                PageRequest.of(0, 10), 0);

        when(paymentCardRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(paymentCardPage);

        Page<ResponsePaymentCardDto> result =
                paymentCardService.getPaymentCards(Pageable.ofSize(10), null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(paymentCardRepository).findAll(any(Specification.class), any(PageRequest.class));
        verifyNoInteractions(paymentCardMapper);
    }

    @Test
    void getPaymentCards_ShouldReturnPageWithFilters() {
        Page<PaymentCard> paymentCardPage = new PageImpl<>(List.of(paymentCard),
                PageRequest.of(0, 10), 1);

        when(paymentCardRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(paymentCardPage);
        when(paymentCardMapper.toDto(paymentCard)).thenReturn(responsePaymentCardDto);

        Page<ResponsePaymentCardDto> result = paymentCardService
                .getPaymentCards(Pageable.ofSize(10), "Roman", "Sidorchuk");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(paymentCardRepository).findAll(any(Specification.class), any(PageRequest.class));
        verify(paymentCardMapper).toDto(paymentCard);
    }

    @Test
    void getPaymentCardById_ShouldThrowException_WhenNotFound() {
        paymentCard.setId(99L);

        when(paymentCardRepository.findById(paymentCard.getId())).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                paymentCardService.getPaymentCardById(paymentCard.getId()));

        verify(paymentCardRepository).findById(paymentCard.getId());
        verifyNoInteractions(paymentCardMapper);
    }

    @Test
    void getPaymentCardById_ShouldReturnPaymentCard_WhenSuccessful() {
        paymentCard.setId(1L);
        paymentCard.setHolder("RAMAN SIDARCHUK");

        when(paymentCardRepository.findById(paymentCard.getId())).thenReturn(Optional.of(paymentCard));
        when(paymentCardMapper.toDto(paymentCard)).thenReturn(responsePaymentCardDto);

        ResponsePaymentCardDto result = paymentCardService.getPaymentCardById(paymentCard.getId());

        assertNotNull(result);
        assertEquals(responsePaymentCardDto.getId(), result.getId());
        assertEquals(responsePaymentCardDto.getHolder(), result.getHolder());

        verify(paymentCardRepository).findById(1L);
        verify(paymentCardMapper).toDto(paymentCard);
    }

    @Test
    void getPaymentCardsByUserId_ShouldReturnListOfUserPaymentCards_WhenSuccessful() {
        responsePaymentCardDto.setUserId(1L);
        user.setId(1L);
        paymentCard.setUser(user);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(paymentCardRepository.findByUserId(user.getId())).thenReturn(List.of(paymentCard));
        when(paymentCardMapper.toDtoList(List.of(paymentCard))).thenReturn(List.of(responsePaymentCardDto));

        List<ResponsePaymentCardDto> result = paymentCardService.getPaymentCardsByUserId(user.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(responsePaymentCardDto.getUserId(), result.getFirst().getUserId());

        verify(userRepository).findById(user.getId());
        verify(paymentCardRepository).findByUserId(user.getId());
        verify(paymentCardMapper).toDtoList(List.of(paymentCard));
    }

    @Test
    void getPaymentCardsByUserId_ShouldThrowException_WhenUserNotFound() {
        user.setId(99L);

        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                paymentCardService.getPaymentCardsByUserId(user.getId()));

        verify(userRepository).findById(user.getId());
        verifyNoInteractions(paymentCardRepository);
        verifyNoInteractions(paymentCardMapper);
    }

    @Test
    void  getPaymentCardsByUserId_ShouldReturnEmptyList() {
        user.setId(1L);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(paymentCardRepository.findByUserId(user.getId())).thenReturn(List.of());

        List<ResponsePaymentCardDto> result = paymentCardService.getPaymentCardsByUserId(user.getId());

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findById(1L);
        verify(paymentCardRepository).findByUserId(1L);
    }

    @Test
    void activatePaymentCard_WhenSuccessful() {
        paymentCard.setId(1L);
        user.setId(1L);
        paymentCard.setUser(user);

        when(paymentCardRepository.findById(paymentCard.getId())).thenReturn(Optional.of(paymentCard));
        when(cacheManager.getCache("user")).thenReturn(cache);

        paymentCardService.activatePaymentCard(paymentCard.getId());

        verify(paymentCardRepository).setActive(paymentCard.getId(), true);
        verify(cacheManager).getCache("user");
    }

    @Test
    void activatePaymentCard_ShouldThrowException_WhenNotFound() {
        paymentCard.setId(1L);

        when(paymentCardRepository.findById(paymentCard.getId())).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                paymentCardService.activatePaymentCard(paymentCard.getId())
        );

        verify(paymentCardRepository).findById(paymentCard.getId());
        verifyNoMoreInteractions(paymentCardRepository);
        verifyNoInteractions(cacheManager);
    }

    @Test
    void deactivatePaymentCard_ShouldReturnUpdatedRow_WhenSuccessful() {
        paymentCard.setId(1L);
        user.setId(1L);
        paymentCard.setUser(user);

        when(paymentCardRepository.findById(paymentCard.getId())).thenReturn(Optional.of(paymentCard));
        when(cacheManager.getCache("user")).thenReturn(cache);

        paymentCardService.deactivatePaymentCard(paymentCard.getId());

        verify(paymentCardRepository).setActive(paymentCard.getId(), false);
        verify(cacheManager).getCache("user");
    }

    @Test
    void deactivatePaymentCard_ShouldThrowException_WhenNotFound() {
        paymentCard.setId(1L);

        when(paymentCardRepository.findById(paymentCard.getId())).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () ->
                paymentCardService.deactivatePaymentCard(paymentCard.getId())
        );

        verify(paymentCardRepository).findById(paymentCard.getId());
        verifyNoMoreInteractions(paymentCardRepository);
        verifyNoInteractions(cacheManager);
    }

    @Test
    void deactivatePaymentCardsByUserId_WhenUserHasCards() {
        user.setId(1L);
        paymentCard.setId(10L);

        when(paymentCardRepository.findIdsByUserId(user.getId())).thenReturn(List.of(paymentCard.getId()));
        when(cacheManager.getCache("paymentCard")).thenReturn(cache);

        paymentCardService.deactivatePaymentCardsByUserId(user.getId());

        verify(paymentCardRepository).findIdsByUserId(user.getId());
        verify(paymentCardRepository).deactivateByUserId(user.getId());
        verify(cacheManager).getCache("paymentCard");
    }

    @Test
    void deactivatePaymentCardsByUserId_WhenUserHasNoCards() {
        user.setId(1L);

        when(paymentCardRepository.findIdsByUserId(user.getId())).thenReturn(List.of());

        paymentCardService.deactivatePaymentCardsByUserId(user.getId());

        verify(paymentCardRepository).findIdsByUserId(user.getId());
        verify(paymentCardRepository).deactivateByUserId(user.getId());
        verifyNoInteractions(cacheManager);
    }

    @Test
    void deactivatePaymentCard_WhenUserCacheIsNull() {
        paymentCard.setId(1L);
        user.setId(5L);
        paymentCard.setUser(user);

        when(paymentCardRepository.findById(paymentCard.getId())).thenReturn(Optional.of(paymentCard));
        when(cacheManager.getCache("user")).thenReturn(null);

        paymentCardService.deactivatePaymentCard(paymentCard.getId());

        verify(paymentCardRepository).findById(paymentCard.getId());
        verify(paymentCardRepository).setActive(paymentCard.getId(), false);
        verify(cacheManager).getCache("user");
    }

}
