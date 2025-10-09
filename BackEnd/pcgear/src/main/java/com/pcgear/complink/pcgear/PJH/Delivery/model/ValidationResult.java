package com.pcgear.complink.pcgear.PJH.Delivery.model;

import lombok.Data;

@Data
public class ValidationResult {
        private final boolean isValid;
        private final String message;
}
