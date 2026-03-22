package com.pcgear.complink.pcgear.Delivery.model.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraphQLReq {
        private String query;
        private Object variables;
}
