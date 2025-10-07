package com.pcgear.complink.pcgear.PJH.SMS;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SendOneRequestDto {
        private Integer orderId;
        private String customerPhoneNumber;
        private String paymentLink;
        private BigDecimal grandAmount;
}
