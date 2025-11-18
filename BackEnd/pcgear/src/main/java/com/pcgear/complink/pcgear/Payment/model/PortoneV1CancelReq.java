package com.pcgear.complink.pcgear.Payment.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class PortoneV1CancelReq {
        private final String imp_uid;
        private final String reason;

}
