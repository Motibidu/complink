package com.pcgear.complink.pcgear.Order.model;

import java.time.LocalDateTime;

import com.pcgear.complink.pcgear.Assembly.AssemblyStatus;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AssemblyQueueRespDto {
        private Integer orderId;
        private String customerName;
        private String assemblyWorkerId;
        private OrderStatus orderStatus;
        private AssemblyStatus assemblyStatus;
        private LocalDateTime paidAt;

        public AssemblyQueueRespDto(Order order) {
                this.orderId = order.getOrderId();
                this.customerName = order.getCustomer().getCustomerName();
                this.assemblyWorkerId = order.getAssemblyWorkerId();
                this.assemblyStatus = order.getAssemblyStatus();
                this.orderStatus= order.getOrderStatus();
                this.paidAt = order.getPaidAt();
        }
}
