package com.pcgear.complink.pcgear.Delivery.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AccessTokenResp {
        private String access_token;

        private String expires_in;

        private String token_type;

}
