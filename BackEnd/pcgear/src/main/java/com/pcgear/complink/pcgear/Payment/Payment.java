package com.pcgear.complink.pcgear.Payment;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.pcgear.complink.pcgear.Payment.model.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Payment {

        @Id
        String paymentId;

        String userId;

        Integer amount;

        String paymentMethod;

        @Enumerated(EnumType.STRING)
        PaymentStatus paymentStatus;

        LocalDateTime paidAt;

        @CreatedDate
        LocalDateTime createdAt;

}
