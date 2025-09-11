package com.pcgear.complink.pcgear.PJH.Payment;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PortoneWebhookRequestDto {

        @JsonProperty("imp_uid")
        private String impUid;

        // 'merchant_uid' 키를 'merchantUid' 필드에 매핑합니다.
        @JsonProperty("merchant_uid")
        private String merchantUid;

        // 웹훅 이벤트 타입을 받을 필드 (예: "vbank_paid", "paid", "ready" 등)
        private String status;

}
