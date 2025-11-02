package com.pcgear.complink.pcgear.Item;

import jakarta.persistence.AttributeConverter;

public class ItemCategoryConverter implements AttributeConverter<ItemCategory, String> {

        @Override
        public String convertToDatabaseColumn(ItemCategory category) {
                if (category == null) {
                        return null;
                }
                return category.getKoreanName();
        }

        @Override
        public ItemCategory convertToEntityAttribute(String dbData) {
                if (dbData == null) {
                        return null;
                }
                return ItemCategory.fromDbData(dbData);
        }

}
