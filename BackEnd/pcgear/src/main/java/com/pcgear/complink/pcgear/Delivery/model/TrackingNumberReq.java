package com.pcgear.complink.pcgear.Delivery.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingNumberReq {
        private Integer orderId;
        private String customerId;
        private String trackingNumber;
        private String carrierId;

}
