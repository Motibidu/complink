package com.pcgear.complink.pcgear.Assembly;

public enum AssemblyStatus {

    // 1. 작업 시작 대기 중 (리스트에서 사용)
    QUEUE("작업 대기"),

    // 2. 부품 확인 및 일련번호(S/N) 입력 단계
    INSPECTING("부품 검수 중"),

    // 3. 조립 완료, 기능 테스트 및 BIOS/OS 설치 완료 단계
    ASSEMBLY_COMPLETE("조립 및 BIOS설치 완료"),

    // 4. 출고 전 최종 포장 및 운송장 번호 입력을 기다리는 단계
    SHIPPING_WAIT("운송장 등록 대기"),

    // 5. 모든 절차 완료 후 고객에게 출고된 상태
    COMPLETED("출고 완료");

    private final String label;

    AssemblyStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public AssemblyStatus nextStatus() {
        return switch (this) {
            case QUEUE -> INSPECTING;
            case INSPECTING -> ASSEMBLY_COMPLETE;
            case ASSEMBLY_COMPLETE -> SHIPPING_WAIT;
            case SHIPPING_WAIT -> COMPLETED;
            case COMPLETED -> null;
        };
    }
}
