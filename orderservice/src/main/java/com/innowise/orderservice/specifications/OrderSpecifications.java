package com.innowise.orderservice.specifications;

import com.innowise.orderservice.entity.Order;
import com.innowise.orderservice.enums.Status;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class OrderSpecifications {

    public static Specification<Order> hasDate(Instant startDate, Instant endDate) {
        return (((root, query, criteriaBuilder) -> {
            if (startDate != null && endDate != null) {
                return criteriaBuilder.between(root.get("createdAt"), startDate, endDate);
            } else if (startDate != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            } else if (endDate != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDate);
            } else {
                return criteriaBuilder.conjunction();
            }
        }));
    }

    public static Specification<Order> hasStatus(Status status) {
        return (((root, query, criteriaBuilder) ->
                (status!=null) ?
                        criteriaBuilder.equal(root.get("status"), status) :
                        criteriaBuilder.conjunction()));
    }

}

