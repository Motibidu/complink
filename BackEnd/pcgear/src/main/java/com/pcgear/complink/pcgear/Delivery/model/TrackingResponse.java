package com.pcgear.complink.pcgear.Delivery.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * GraphQL 배송 조회 (track Query) API의 최종 응답 구조입니다.
 * 상세한 배송 이벤트 이력 (lastEvent, events)을 포함하도록 업데이트되었습니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingResponse {
        private TrackingData data;
        private List<Error> errors;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Error {
                private String message;
                private Map<String, Object> extensions;

                @Data
                @NoArgsConstructor
                @AllArgsConstructor
                public static class Extension {
                        private String code;
                }
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class TrackingData {
                // 'track' 쿼리의 응답 페이로드
                private Track track;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Track {
                // 최종 이벤트 정보 (JSON의 lastEvent 필드)
                private LastEvent lastEvent;

                // 전체 배송 이벤트 목록 (JSON의 events 필드)
                private Events events;
        }

        /**
         * 마지막 이벤트 (lastEvent)의 상세 정보
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class LastEvent {
                private String time;
                private EventStatus status;
                private String description;
        }

        /**
         * 배송 이벤트의 상태 상세 정보 (code, name)
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class EventStatus {
                private String code; // 상태 코드 (예: DELIVERED)
                private String name; // 상태 이름 (예: 배송완료)
        }

        /**
         * 전체 이벤트 목록의 루트 객체
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Events {
                private List<EventEdge> edges;
        }

        /**
         * 이벤트 목록의 엣지 (Edge) 구조. 실제 이벤트 정보는 'node' 필드에 담겨 있습니다.
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class EventEdge {
                private EventNode node;
        }

        /**
         * 단일 배송 이벤트 (Node)의 상세 정보
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class EventNode {
                private String time;
                private EventStatus status;
                private String description;
        }
}
