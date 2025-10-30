package com.pcgear.complink.pcgear.Payment.model;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentInfoDto {
        Map<String, Object> paymentInfo;
        String paymentId;
        String userId;
        Integer amount;

}
