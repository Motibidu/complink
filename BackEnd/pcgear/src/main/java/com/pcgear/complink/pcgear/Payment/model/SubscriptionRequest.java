package com.pcgear.complink.pcgear.Payment.model;

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
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequest {
        private String billingKey;
        private String billingKeyMethod;
        private Integer amount;
}
