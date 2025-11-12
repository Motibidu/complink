package com.pcgear.complink.pcgear.Item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import java.util.List;

@Data
@Builder
@NoArgsConstructor // ⬅️ [필수] Jackson이 역직렬화(Redis -> Java)할 때 필요
@AllArgsConstructor // ⬅️ [권장] Builder 사용 시 필요
public class ItemPageDto {

        // 1. 실제 데이터 (React의 pageData.content)
        private List<Item> content;

        // 2. 페이징 메타데이터
        private int totalPages; // 총 페이지 수
        private long totalElements; // 총 데이터 개수
        private int number; // 현재 페이지 번호 (0-based)
        private int size; // 페이지 크기
        private boolean first; // 첫 페이지 여부
        private boolean last; // 마지막 페이지 여부

        /**
         * [편의 메서드]
         * Spring Data의 Page<Item> 객체를
         * 우리가 만든 ItemPageDto로 변환합니다.
         */
        public ItemPageDto(Page<Item> page) {
                this.content = page.getContent();
                this.totalPages = page.getTotalPages();
                this.totalElements = page.getTotalElements();
                this.number = page.getNumber();
                this.size = page.getSize();
                this.first = page.isFirst();
                this.last = page.isLast();
        }
}