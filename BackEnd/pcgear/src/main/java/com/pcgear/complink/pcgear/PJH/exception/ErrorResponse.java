package com.pcgear.complink.pcgear.PJH.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    private final String message;
    // 필요하다면 에러 코드, 타임스탬프 등 필드 추가 가능
}
