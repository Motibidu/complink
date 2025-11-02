package com.pcgear.complink.pcgear.Order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.pcgear.complink.pcgear.Item.Item;
import com.pcgear.complink.pcgear.Item.ItemCategory;

@Getter
@Setter
@Entity
@ToString
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderItemId;

    @JsonBackReference
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id") // DB 상의 외래 키 컬럼 이름
    private Item item;

    @Enumerated(EnumType.STRING)
    private ItemCategory itemCategory;

    private String serialNum;

    private Boolean serialNumRequired;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalPrice;

}
