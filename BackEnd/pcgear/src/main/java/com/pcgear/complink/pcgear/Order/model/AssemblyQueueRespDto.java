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
        private String managerName;
        private OrderStatus orderStatus;
        private AssemblyStatus assemblyStatus;
        private LocalDateTime paidAt;

        public AssemblyQueueRespDto(Integer orderId, String customerName, String managerName,
                        OrderStatus orderStatus, AssemblyStatus assemblyStatus, LocalDateTime paidAt) {
                this.orderId = orderId;
                this.customerName = customerName;
                this.managerName = managerName; // manager가 없으면 null이 들어옴
                this.orderStatus = orderStatus;
                this.assemblyStatus = assemblyStatus;
                this.paidAt = paidAt;
        }
}
