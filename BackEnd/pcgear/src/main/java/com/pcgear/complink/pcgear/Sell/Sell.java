package com.pcgear.complink.pcgear.Sell;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.pcgear.complink.pcgear.Customer.Customer;
import com.pcgear.complink.pcgear.Manager.Manager;
import com.pcgear.complink.pcgear.Order.model.Order;
import com.pcgear.complink.pcgear.User.entity.UserEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sells")
@EntityListeners(AuditingEntityListener.class)
public class Sell {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer sellId;

        private LocalDateTime sellDate;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "order_id") // 실제 DB 컬럼명
        private Order order;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "customer_id")
        private Customer customer;

        // [변경 3] String managerId -> Manager(Member) 객체
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "id")
        private UserEntity manager;

        private BigDecimal totalAmount;

        private BigDecimal vatAmount;

        private BigDecimal grandAmount;

        private String memo;

        @CreatedDate
        private LocalDateTime createdAt;
}
