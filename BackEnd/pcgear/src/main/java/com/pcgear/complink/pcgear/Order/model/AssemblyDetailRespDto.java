package com.pcgear.complink.pcgear.Order.model;

import java.util.List;

import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;
import com.pcgear.complink.pcgear.Customer.Customer;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class AssemblyDetailRespDto {
        private Integer orderId;
        private Customer customer;
        private OrderStatus orderStatus;
        private AssemblyStatus assemblyStatus;
        private List<OrderItem> orderItems;

}
