package com.pcgear.complink.pcgear.Order.model;

import java.util.List;

import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;
import com.pcgear.complink.pcgear.Customer.Customer;
import com.querydsl.core.annotations.QueryProjection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssemblyDetailRespDto {
        private Integer orderId;
        private Customer customer;
        private OrderStatus orderStatus;
        private AssemblyStatus assemblyStatus;
        private List<OrderItem> orderItems;

        @QueryProjection
        public AssemblyDetailRespDto(Integer orderId, OrderStatus orderStatus, AssemblyStatus assemblyStatus,
                        Customer customer, List<OrderItem> orderItems) {
                this.orderId = orderId;
                this.orderStatus = orderStatus;
                this.assemblyStatus = assemblyStatus;
                this.customer = customer;
                this.orderItems = orderItems;
        }

}
