package com.pcgear.complink.pcgear.Payment.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PortoneV1AccessTokenReq {
        private final String imp_key;
        private final String imp_secret;
}
