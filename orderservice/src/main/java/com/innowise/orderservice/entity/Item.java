package com.innowise.orderservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Table(name = "items")
@Getter
@Setter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE items SET deleted = true WHERE id = ?")
@SQLRestriction(value = "deleted=false")
public class Item extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private BigDecimal price;

    private Boolean deleted = false;

}
