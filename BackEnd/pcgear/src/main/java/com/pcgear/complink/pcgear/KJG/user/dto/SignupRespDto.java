package com.pcgear.complink.pcgear.KJG.user.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class SignupRespDto {
        private String id;
        private String name;
        private String email;
        private LocalDateTime requestDate;
}
