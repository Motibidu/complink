package com.pcgear.complink.pcgear.Item;

public enum ItemCategory {
        // 일련번호가 필수인 부품 (REQUIRED=true)
        CPU("CPU", true),
        MAINBOARD("메인보드", true),
        RAM("RAM", true),
        SSD("SSD", true),
        VGA("그래픽카드", true),
        PSU("파워서플라이", true),

        // 일련번호가 필수가 아닌 부품 (REQUIRED=false)
        CASE("케이스", false),
        COOLER("CPU 쿨러", false),
        FAN("시스템 팬", false);

        private final String koreanName;
        private final boolean isRequired;

        ItemCategory(String koreanName, boolean isRequired) {
                this.koreanName = koreanName;
                this.isRequired = isRequired;
        }

        // ⭐️ 일련번호 필수 여부 로직을 enum 자체에 포함
        public boolean isSerialNumRequired() {
                return this.isRequired;
        }

        // UI에 보여줄 이름을 가져오는 메서드
        public String getKoreanName() {
                return this.koreanName;
        }

        public static ItemCategory fromDbData(String dbData) {
                for (ItemCategory category : ItemCategory.values()) {
                        if (category.getKoreanName().equals(dbData)) {
                                return category;
                        }
                }
                throw new IllegalArgumentException("Unknown ItemCategory: " + dbData);
        }

}
