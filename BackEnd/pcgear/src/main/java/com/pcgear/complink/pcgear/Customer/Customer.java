package com.pcgear.complink.pcgear.Customer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@ToString
@NoArgsConstructor
@Table(name = "customers")
@EntityListeners(AuditingEntityListener.class)
public class Customer {

    @Id
    private String customerId;

    private String customerName;

    private String phoneNumber;

    private String email;

    private String address;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
