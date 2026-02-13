package com.pcgear.complink.pcgear.Delivery.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 택배사 코드와 표시명 매핑 Enum
 * - code: API 연동 및 DB 저장에 사용되는 실제 코드값
 * - displayName: 사용자에게 표시되는 한글 명칭
 */
@Getter
@RequiredArgsConstructor
public enum CarrierCode {

    CJ_LOGISTICS("kr.cjlogistics", "CJ 대한통운"),
    HANJIN("kr.hanjin", "한진택배"),
    LOTTE("kr.lotte", "롯데택배"),
    LOGEN("kr.logen", "로젠택배"),
    POST_OFFICE("kr.epost", "우체국택배"),
    KDEXP("kr.kdexp", "경동택배"),
    CHUNIL("kr.chunil", "천일택배"),
    HDEXP("kr.hdexp", "합동택배"),
    DAESIN("kr.daesin", "대신택배"),
    ILYANG("kr.ilyang", "일양로지스"),
    KYUNGDONG("kr.kyungdong", "경동택배"),
    CVSNET("kr.cvsnet", "편의점택배");

    private final String code;
    private final String displayName;

    /**
     * 코드값으로 Enum 찾기
     * @param code 택배사 코드 (예: "kr.cjlogistics")
     * @return 해당하는 CarrierCode Enum
     * @throws IllegalArgumentException 존재하지 않는 코드인 경우
     */
    public static CarrierCode fromCode(String code) {
        for (CarrierCode carrier : values()) {
            if (carrier.code.equals(code)) {
                return carrier;
            }
        }
        throw new IllegalArgumentException("Unknown carrier code: " + code);
    }

    /**
     * 코드값으로 표시명 가져오기 (null-safe)
     * @param code 택배사 코드
     * @return 표시명, 존재하지 않으면 코드값 그대로 반환
     */
    public static String getDisplayName(String code) {
        if (code == null) {
            return null;
        }
        try {
            return fromCode(code).getDisplayName();
        } catch (IllegalArgumentException e) {
            return code; // 매핑되지 않은 코드는 그대로 반환
        }
    }
}
