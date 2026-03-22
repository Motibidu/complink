package com.pcgear.complink.pcgear.Order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;
import com.pcgear.complink.pcgear.Customer.Customer;
import com.pcgear.complink.pcgear.Manager.Manager;
import com.pcgear.complink.pcgear.User.entity.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_order_status", columnList = "order_status"),
        @Index(name = "idx_orders_date_status", columnList = "order_date, order_status"),
        @Index(name = "idx_orders_customer_status", columnList = "customer_id, order_status")
})
@EntityListeners(AuditingEntityListener.class) // 생성/수정 시간 자동 감지를 위한 리스너
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    private UserEntity manager;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    private String paymentLink;

    private String merchantUid;

    private String impUid;

    private LocalDate deliveryDate;

    private BigDecimal totalAmount;

    private BigDecimal vatAmount;

    private BigDecimal grandAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private AssemblyStatus assemblyStatus;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime paidAt;

    @JsonManagedReference
    @ToString.Exclude
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Builder 사용 시 ArrayList 초기화 유지
    private List<OrderItem> orderItems = new ArrayList<>();

    // == 연관관계 편의 메서드 ==//
    public void addItem(OrderItem orderItem) {
        if (orderItems == null) {
            orderItems = new ArrayList<>();
        }
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}
