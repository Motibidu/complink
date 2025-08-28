package com.pcgear.complink.pcgear.PJH.Order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Table(name = "managers")
@EntityListeners(AuditingEntityListener.class)
public class Manager {

    @Id
    @Column(name = "manager_id")
    private String managerId; // Long -> Integer

    @Column(name = "manager_name", nullable = false)
    private String managerName;

    @Column(unique = true)
    private String email;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

