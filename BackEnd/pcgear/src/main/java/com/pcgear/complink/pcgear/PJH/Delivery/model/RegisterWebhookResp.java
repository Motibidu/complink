package com.pcgear.complink.pcgear.PJH.Delivery.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class RegisterWebhookResp {
        private WebhookRegisterResponse data;
        private List<Error> errors;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class WebhookRegisterResponse {
                // 실제 응답 필드 이름과 일치해야 합니다.
                private Boolean registerTrackWebhook;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Error {
                private String message;
        }
}
