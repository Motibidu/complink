package com.pcgear.complink.pcgear.PJH.Customer;

import lombok.Getter;

@Getter
public class CustomerDto {
        private final String customerId;
        private final String customerName;
        private final String customerAddress;
        private final String phoneNumber;

        // Customer 엔티티를 받아서 DTO를 생성하는 생성자
        public CustomerDto(Customer customer) {
                this.customerId = customer.getCustomerId();
                this.customerName = customer.getCustomerName();
                this.customerAddress = customer.getAddress();
                this.phoneNumber = customer.getPhoneNumber();
        }
}
