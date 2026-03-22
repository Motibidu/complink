package com.pcgear.complink.pcgear.Delivery.enums;

public enum RetryStatus {
        /**
         * 재시도 대상.
         * 스케줄러가 'nextRunAt' 시간을 확인하여 주기적으로 호출함.
         */
        ACTIVE,

        /**
         * 재처리 성공.
         * 메인 테이블의 상태가 SUCCESS로 변경되었으며, 더 이상 시도하지 않음.
         */
        COMPLETED,

        /**
         * 최종 실패 (포기).
         * 'maxRetries'를 모두 소진함. 운영자의 수동 확인이 필요한 상태.
         */
        EXHAUSTED
}
