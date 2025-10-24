package com.pcgear.complink.pcgear.Assembly;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AssemblyStatusConverter implements AttributeConverter<AssemblyStatus, String> {

    @Override
    public String convertToDatabaseColumn(AssemblyStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    /**
     * 데이터베이스 String 컬럼 값을 AssemblyStatus Enum으로 변환합니다.
     * * @param dbData 데이터베이스에서 읽어온 문자열 (예: "QUEUE")
     * @return 매핑된 AssemblyStatus Enum
     */
    @Override
    public AssemblyStatus convertToEntityAttribute(String dbData) {
        
        // 1. NULL 또는 빈 문자열 처리
        if (dbData == null || dbData.trim().isEmpty()) {
            // DB에 상태가 설정되지 않은 기존 레코드 또는 신규 레코드의 경우,
            // 기본 작업 대기 상태인 QUEUE를 반환하여 안전하게 로드합니다.
            System.err.println("경고: Order 테이블의 AssemblyStatus 컬럼이 NULL 또는 빈 문자열이었습니다. 기본값 (QUEUE)으로 설정합니다.");
            return AssemblyStatus.QUEUE;
        }

        try {
            // 2. 유효한 Enum 값 변환 시도
            return AssemblyStatus.valueOf(dbData.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // 3. 알 수 없는 값이 저장되어 있을 때 예외 처리
            System.err.println("DB에 저장된 알 수 없는 AssemblyStatus 값: " + dbData + ". 기본값 (QUEUE)으로 설정합니다.");
            // 치명적인 오류 대신 기본값 QUEUE를 반환하여 앱이 중단되지 않도록 방어 로직을 적용합니다.
            // 필요하다면 여기서 로깅 후, null을 반환하여 엔티티 필드에 null을 허용하거나 예외를 던질 수 있습니다.
            return AssemblyStatus.QUEUE; 
        }
    }
}
