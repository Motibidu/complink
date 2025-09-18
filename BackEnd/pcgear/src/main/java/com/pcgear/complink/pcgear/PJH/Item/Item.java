package com.pcgear.complink.pcgear.PJH.Item;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "items")
@Getter
@Setter
public class Item {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer itemId;

        @NotBlank(message = "품목명은 필수 입력 항목입니다.")
        @Size(max = 255, message = "품목명은 255자를 초과할 수 없습니다.")
        @Schema(description = "품목명", example = "GIGABYTE 지포스 RTX 4070 SUPER")
        private String itemName;

        @NotBlank(message = "카테고리는 필수 입력 항목입니다.")
        private String category;

        private Integer QuantityOnHand;

        @NotNull(message = "입고단가는 필수 입력 항목입니다.")
        @PositiveOrZero(message = "입고단가는 0 또는 양수여야 합니다.")
        private Integer purchasePrice;

        @NotNull(message = "출고단가는 필수 입력 항목입니다.")
        @PositiveOrZero(message = "출고단가는 0 또는 양수여야 합니다.")
        private Integer sellingPrice;
}
