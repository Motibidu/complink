package com.pcgear.complink.pcgear.Order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;
import com.pcgear.complink.pcgear.Assembly.AssemblyStatusConverter;
import com.pcgear.complink.pcgear.Customer.Customer;
import com.pcgear.complink.pcgear.Manager.Manager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@ToString
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class) // 생성/수정 시간 자동 감지를 위한 리스너
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Manager manager;

    private String paymentLink;

    private String merchantUid;

    private String impUid;

    private LocalDate deliveryDate;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "vat_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal vatAmount;

    @Column(name = "grand_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal grandAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private String assemblyWorkerId; // 조립 담당 직원 ID (새로 추가)

    private AssemblyStatus assemblyStatus = AssemblyStatus.QUEUE; // 기본값 '작업 대기'

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private LocalDateTime paidAt;

    @JsonManagedReference
    @ToString.Exclude
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    // == 연관관계 편의 메서드 ==//
    public void addItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}
