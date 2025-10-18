package com.pcgear.complink.pcgear.Order.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderStatusConverter implements AttributeConverter<OrderStatus, Integer> {

        @Override
        public Integer convertToDatabaseColumn(OrderStatus attribute) {
                // PaymentStatus Enum -> DB 컬럼 (Integer)으로 변환
                if (attribute == null) {
                        return null;
                }
                return attribute.getCode(); // Enum의 code 값을 DB에 저장
        }

        @Override
        public OrderStatus convertToEntityAttribute(Integer dbData) {
                // DB 컬럼 (Integer) -> PaymentStatus Enum으로 변환
                if (dbData == null) {
                        return null;
                }
                return OrderStatus.fromCode(dbData); // DB의 숫자로 Enum을 찾음
        }
}
