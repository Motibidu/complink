package com.pcgear.complink.pcgear.PJH.Payment;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Setter;

@Entity
@Table(name = "subscriptions")
@Setter
@EntityListeners(AuditingEntityListener.class)
public class Subscription {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Integer subscriptionId;

        String userId;
        String billingKey;
        String status;

        LocalDateTime startTime;
        LocalDateTime nextBillingTime;

        @CreatedDate
        LocalDateTime createdAt;

        @LastModifiedDate
        LocalDateTime updatedAt;
}
