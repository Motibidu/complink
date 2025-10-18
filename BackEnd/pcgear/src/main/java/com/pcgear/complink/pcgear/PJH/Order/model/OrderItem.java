package com.pcgear.complink.pcgear.PJH.Order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Getter
@Setter
@Entity
@ToString
@Table(name = "order_items")
public class OrderItem {

    @JsonBackReference
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private Integer itemId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderItemId;

    private String category;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPrice;

}
