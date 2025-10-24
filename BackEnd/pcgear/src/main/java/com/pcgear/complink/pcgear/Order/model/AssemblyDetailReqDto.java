package com.pcgear.complink.pcgear.Order.model;

import java.util.List;

import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;

import lombok.Getter;

@Getter
public class AssemblyDetailReqDto {
        private AssemblyStatus nextAssemblyStatus;
        private List<OrderItem> orderItems;
}
