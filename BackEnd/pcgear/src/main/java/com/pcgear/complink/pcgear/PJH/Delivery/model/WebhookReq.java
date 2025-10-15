package com.pcgear.complink.pcgear.PJH.Delivery.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WebhookReq {
        private String trackingNumber;
        private String carrierId;
}
