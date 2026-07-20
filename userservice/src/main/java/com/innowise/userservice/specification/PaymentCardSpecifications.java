package com.innowise.userservice.specification;

import com.innowise.userservice.entity.PaymentCard;
import com.innowise.userservice.entity.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecifications {

    public static Specification<PaymentCard> hasUserName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            Join<PaymentCard, User> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("name"), name);
        };
    }

    public static Specification<PaymentCard> hasUserSurname(String surname) {
        return (root, query, criteriaBuilder) -> {
            if (surname == null || surname.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            Join<PaymentCard, User> userJoin = root.join("user");
            return criteriaBuilder.equal(userJoin.get("surname"), surname);
        };
    }
}