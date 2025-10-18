package com.pcgear.complink.pcgear.Payment.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SubscriptionRequest {
        private String billingKey;
        private String orderName;
        private Integer amount;
}
