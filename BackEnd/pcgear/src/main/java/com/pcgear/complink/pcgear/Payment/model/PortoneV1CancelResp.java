package com.pcgear.complink.pcgear.Payment.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PortoneV1CancelResp {
        private PortoneCancelData response;
        private Integer code;
        private String message;

        @Getter
        @Setter
        @ToString
        public static class PortoneCancelData {
                private String cancel_amount;
        }
}
