package com.pcgear.complink.pcgear.Payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortoneV1AccessTokenResp {
        private PortOneTokenData response;

        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public class PortOneTokenData {
                private String access_token;
        }

}
