package com.innowise.userservice.repository;

import com.innowise.userservice.entity.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    @Modifying
    @Query("UPDATE PaymentCard p SET p.active = :active WHERE p.id = :id")
    void setActive(Long id, Boolean active);

    List<PaymentCard> findByUserId(Long id);

    @Modifying
    @Query ("UPDATE PaymentCard p SET p.active = false WHERE p.user.id = :id")
    void deactivateByUserId(Long id);

    @Query("SELECT COUNT(*) FROM User u WHERE u.id = :id")
    int countByUserId(Long id);

    @Query("SELECT p.id FROM PaymentCard p WHERE p.user.id = :userId")
    List<Long> findIdsByUserId(Long userId);

}
