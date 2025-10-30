package com.pcgear.complink.pcgear.Payment.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SingleInquiryResponse {

        private ResponseData response;

        @Getter
        @Setter
        @ToString
        public static class ResponseData {
                private BigDecimal amount;

                private String buyer_tel;

                private String status;
        }

}
