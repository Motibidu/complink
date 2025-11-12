package com.pcgear.complink.pcgear.Manager;

import java.util.List;

import org.springframework.data.domain.Page;

public class ManagerPageDto {
        List<Manager> content;
        private int totalPages; // 총 페이지 수
        private long totalElements; // 총 데이터 개수
        private int number; // 현재 페이지 번호 (0-based)
        private int size; // 페이지 크기
        private boolean first; // 첫 페이지 여부
        private boolean last; // 마지막 페이지 여부

        public ManagerPageDto(Page<Manager> page) {
                this.content = page.getContent();
                this.totalPages = page.getTotalPages();
                this.totalElements = page.getTotalElements();
                this.number = page.getNumber();
                this.size = page.getSize();
                this.first = page.isFirst();
                this.last = page.isLast();
        }

}
