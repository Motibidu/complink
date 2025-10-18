package com.pcgear.complink.pcgear.Payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AccessTokenResponse {
        private int code;
        private String message;
        private ResponseData response; // 내부 객체를 위한 DTO

        @Getter
        @Setter
        @ToString
        public static class ResponseData { // static inner class로 정의
                @JsonProperty("access_token")
                private String accessToken;
                private long now;
                @JsonProperty("expired_at")
                private long expiredAt;
        }
}
