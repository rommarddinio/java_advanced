package com.innowise.userservice.service.impl;

import com.innowise.userservice.dto.paymentcard.CreatePaymentCardDto;
import com.innowise.userservice.dto.paymentcard.ResponsePaymentCardDto;
import com.innowise.userservice.dto.paymentcard.UpdatePaymentCardDto;
import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.exception.CardLimitException;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.PaymentCardMapper;
import com.innowise.userservice.repository.PaymentCardRepository;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.service.PaymentCardService;
import com.innowise.userservice.specification.PaymentCardSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class PaymentCardServiceImpl implements PaymentCardService {

    private final UserRepository userRepository;

    private final PaymentCardRepository paymentCardRepository;

    private final PaymentCardMapper paymentCardMapper;

    private final CacheManager cacheManager;

    @Transactional
    @Override
    public ResponsePaymentCardDto createPaymentCard(CreatePaymentCardDto paymentCardDto) {
        userRepository.findById(paymentCardDto.getUserId())
                .orElseThrow(UserNotFoundException::new);
        if (paymentCardRepository.countByUserId(paymentCardDto.getUserId()) >= 5)
            throw new CardLimitException();

        PaymentCard paymentCard = paymentCardMapper.toEntity(paymentCardDto);
        paymentCard.setActive(true);

        return paymentCardMapper.toDto(paymentCardRepository.save(paymentCard));
    }

    @Cacheable(value = "paymentCard", key = "#id")
    @Override
    public ResponsePaymentCardDto getPaymentCardById(Long id) {
        return paymentCardMapper.toDto(paymentCardRepository.findById(id)
                .orElseThrow(CardNotFoundException::new));
    }

    @Override
    public List<ResponsePaymentCardDto> getPaymentCardsByUserId(Long userId) {
        userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        return paymentCardMapper.toDtoList(paymentCardRepository.findByUserId(userId));
    }

    @Override
    public Page<ResponsePaymentCardDto> getPaymentCards(Pageable pageable, String name, String surname) {
        Specification<PaymentCard> specification = Specification.where(PaymentCardSpecifications.hasUserName(name))
                .and(PaymentCardSpecifications.hasUserSurname(surname));

        return paymentCardRepository.findAll(specification, PageRequest
                .of(pageable.getPageNumber(), pageable.getPageSize())).map(paymentCardMapper::toDto);
    }

    @CacheEvict(value = "paymentCard", key = "#id")
    @Transactional
    @Override
    public void activatePaymentCard(Long id) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(CardNotFoundException::new);

        paymentCardRepository.setActive(id, true);

        Cache userCache = cacheManager.getCache("user");
        if (userCache != null) {
            userCache.evict(card.getUser().getId());
        }
    }

    @CacheEvict(value = "paymentCard", key = "#id")
    @Transactional
    @Override
    public void deactivatePaymentCard(Long id) {
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(CardNotFoundException::new);

        paymentCardRepository.setActive(id, false);

        Cache userCache = cacheManager.getCache("user");
        if (userCache != null) {
            userCache.evict(card.getUser().getId());
        }
    }

    @Transactional
    @Override
    public void deactivatePaymentCardsByUserId(Long userId) {
        List<Long> ids = paymentCardRepository.findIdsByUserId(userId);

        paymentCardRepository.deactivateByUserId(userId);

        if (ids != null && !ids.isEmpty()) {
            Cache cardCache = cacheManager.getCache("paymentCard");
            if (cardCache != null) {
                for (Long cardId : ids) {
                    cardCache.evict(cardId);
                }
            }
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "paymentСard", key = "#id"),
            @CacheEvict(value = "user", key = "#paymentCardDto.userId")
    })
    @Transactional
    @Override
    public ResponsePaymentCardDto updatePaymentCard(Long id, UpdatePaymentCardDto paymentCardDto) {
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(CardNotFoundException::new);

        paymentCard.setNumber(paymentCardDto.getNumber());
        paymentCard.setHolder(paymentCardDto.getHolder());
        paymentCard.setExpirationDate(paymentCardDto.getExpirationDate());
        paymentCard.setUser(userRepository.findById(paymentCardDto.getUserId()).
                orElseThrow(UserNotFoundException::new));

        return paymentCardMapper.toDto(paymentCardRepository.save(paymentCard));
    }
}
