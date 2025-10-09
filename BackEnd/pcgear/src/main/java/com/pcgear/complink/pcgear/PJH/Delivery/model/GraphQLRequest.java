package com.pcgear.complink.pcgear.PJH.Delivery.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraphQLRequest {
        private String query;
        private Object variables; // 또는 Map<String, Object>
}
