package com.innowise.paymentservice.entity;

import com.innowise.paymentservice.enums.Status;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "payments")
@Getter
@Setter
public class Payment {

    @Id
    private String id;

    private Long userId;

    private Long orderId;

    private Status status;

    private Instant timestamp = Instant.now();

    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal paymentAmount;

}
