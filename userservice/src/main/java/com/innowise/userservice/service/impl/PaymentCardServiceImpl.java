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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("Creating a new payment card for user with id = {}", paymentCardDto.getUserId());
        userRepository.findById(paymentCardDto.getUserId())
                .orElseThrow(() -> {
                    log.warn("Failed to create card: User not found with id = {}", paymentCardDto.getUserId());
                    return new UserNotFoundException();
                });
        if (paymentCardRepository.countByUserId(paymentCardDto.getUserId()) >= 5) {
            log.warn("Failed to create card: User with id = {} has reached the limit of 5 cards", paymentCardDto.getUserId());
            throw new CardLimitException();
        }

        PaymentCard paymentCard = paymentCardMapper.toEntity(paymentCardDto);
        paymentCard.setActive(true);

        ResponsePaymentCardDto savedCard = paymentCardMapper.toDto(paymentCardRepository.save(paymentCard));
        log.info("Successfully created payment card with id = {} for user id = {}", savedCard.getId(), paymentCardDto.getUserId());
        return savedCard;
    }

    @Cacheable(value = "paymentCard", key = "#id")
    @Override
    public ResponsePaymentCardDto getPaymentCardById(Long id) {
        log.debug("Cache miss: Fetching payment card from database with id = {}", id);
        return paymentCardMapper.toDto(paymentCardRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Payment card not found with id = {}", id);
                    return new CardNotFoundException();
                }));
    }

    @Override
    public List<ResponsePaymentCardDto> getPaymentCardsByUserId(Long userId) {
        log.info("Fetching all payment cards for user with id = {}", userId);
        userRepository.findById(userId).orElseThrow(() -> {
            log.warn("Failed to fetch cards: User not found with id = {}", userId);
            return new UserNotFoundException();
        });

        return paymentCardMapper.toDtoList(paymentCardRepository.findByUserId(userId));
    }

    @Override
    public Page<ResponsePaymentCardDto> getPaymentCards(Pageable pageable, String name, String surname) {
        log.info("Fetching page {} of payment cards with filters - name: {}, surname: {}", pageable.getPageNumber(), name, surname);
        Specification<PaymentCard> specification = Specification.where(PaymentCardSpecifications.hasUserName(name))
                .and(PaymentCardSpecifications.hasUserSurname(surname));

        return paymentCardRepository.findAll(specification, PageRequest
                .of(pageable.getPageNumber(), pageable.getPageSize())).map(paymentCardMapper::toDto);
    }

    @CacheEvict(value = "paymentCard", key = "#id")
    @Transactional
    @Override
    public void activatePaymentCard(Long id) {
        log.info("Activating payment card with id = {}", id);
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Failed to activate card: Card not found with id = {}", id);
                    return new CardNotFoundException();
                });

        paymentCardRepository.setActive(id, true);
        log.info("Payment card with id = {} successfully activated", id);

        Cache userCache = cacheManager.getCache("user");
        if (userCache != null) {
            userCache.evict(card.getUser().getId());
            log.debug("Evicted user cache for user id = {} due to card activation", card.getUser().getId());
        }
    }

    @CacheEvict(value = "paymentCard", key = "#id")
    @Transactional
    @Override
    public void deactivatePaymentCard(Long id) {
        log.info("Deactivating payment card with id = {}", id);
        PaymentCard card = paymentCardRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Failed to deactivate card: Card not found with id = {}", id);
                    return new CardNotFoundException();
                });

        paymentCardRepository.setActive(id, false);
        log.info("Payment card with id = {} successfully deactivated", id);

        Cache userCache = cacheManager.getCache("user");
        if (userCache != null) {
            userCache.evict(card.getUser().getId());
            log.debug("Evicted user cache for user id = {} due to card deactivation", card.getUser().getId());
        }
    }

    @Transactional
    @Override
    public void deactivatePaymentCardsByUserId(Long userId) {
        log.info("Deactivating all payment cards for user with id = {}", userId);
        List<Long> ids = paymentCardRepository.findIdsByUserId(userId);

        paymentCardRepository.deactivateByUserId(userId);
        log.info("Bulk deactivated cards in database for user with id = {}", userId);

        if (ids != null && !ids.isEmpty()) {
            Cache cardCache = cacheManager.getCache("paymentCard");
            if (cardCache != null) {
                for (Long cardId : ids) {
                    cardCache.evict(cardId);
                }
                log.debug("Evicted {} card keys from 'paymentCard' cache for user id = {}", ids.size(), userId);
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
        log.info("Updating payment card with id = {}", id);
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Failed to update card: Card not found with id = {}", id);
                    return new CardNotFoundException();
                });

        paymentCard.setNumber(paymentCardDto.getNumber());
        paymentCard.setHolder(paymentCardDto.getHolder());
        paymentCard.setExpirationDate(paymentCardDto.getExpirationDate());
        paymentCard.setUser(userRepository.findById(paymentCardDto.getUserId()).
                orElseThrow(() -> {
                    log.warn("Failed to update card: Assigned user not found with id = {}", paymentCardDto.getUserId());
                    return new UserNotFoundException();
                }));

        ResponsePaymentCardDto updatedCard = paymentCardMapper.toDto(paymentCardRepository.save(paymentCard));
        log.info("Payment card with id = {} successfully updated", id);
        return updatedCard;
    }
}
