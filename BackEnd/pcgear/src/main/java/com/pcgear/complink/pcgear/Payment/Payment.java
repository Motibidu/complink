package com.pcgear.complink.pcgear.Payment;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Payment {

        @Id
        @Column(name = "payment_id")
        String paymentId;

        @Column(name = "user_id")
        String userId;

        Integer amount;

        @Column(name = "payment_method")
        String paymentMethod;

        String status;

        @Column(name = "paid_at")
        LocalDateTime paidAt;

        @CreatedDate
        @Column(name = "created_at")
        LocalDateTime createdAt;

}
