package com.pcgear.complink.pcgear.Delivery.model;

import com.pcgear.complink.pcgear.Order.model.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ShippingListDto {

        private Integer orderId;
        private String customerName; // From Order -> Customer
        private LocalDateTime createdAt; // From Order
        private String trackingNumber; // From Delivery
        private String carrierId; // From Delivery (코드값: kr.cjlogistics)
        private String carrierDisplayName; // 택배사 표시명 (예: CJ 대한통운)
        private DeliveryStatus deliveryStatus;

        /**
         * JPQL의 "new" 생성자에서 사용할 생성자
         */
        public ShippingListDto(Integer orderId, String customerName, LocalDateTime createdAt,
                        String trackingNumber, String carrierId, DeliveryStatus deliveryStatus) {
                this.orderId = orderId;
                this.customerName = customerName;
                this.createdAt = createdAt;
                this.trackingNumber = trackingNumber;
                this.carrierId = carrierId;
                this.carrierDisplayName = CarrierCode.getDisplayName(carrierId); // 코드 -> 표시명 변환
                this.deliveryStatus = deliveryStatus;
        }
}
