package com.pcgear.complink.pcgear.Sell;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

        private Integer orderId;

        private String customerId;

        private String customerName;

        private String managerId;

        private String managerName;

        private BigDecimal totalAmount;

        private BigDecimal vatAmount;

        private BigDecimal grandAmount;

        private String memo;

        @CreatedDate
        private LocalDateTime createdAt;

}
