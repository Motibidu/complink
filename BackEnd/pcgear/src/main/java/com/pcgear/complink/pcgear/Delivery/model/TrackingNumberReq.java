package com.pcgear.complink.pcgear.Delivery.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TrackingNumberReq {
        private Integer orderId;
        private String customerId;
        private String trackingNumber;
        private String carrierId;
}
