package com.pcgear.complink.pcgear.Item;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "items")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer itemId;

        private String itemName;

        @Enumerated(EnumType.STRING)
        private ItemCategory itemCategory;

        private Integer quantityOnHand;

        private Integer availableQuantity;

        private BigDecimal purchasePrice;

        private BigDecimal sellingPrice;
}
