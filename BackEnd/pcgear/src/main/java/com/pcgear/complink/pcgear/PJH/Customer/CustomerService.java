package com.pcgear.complink.pcgear.PJH.Customer;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pcgear.complink.pcgear.PJH.Order.repository.OrderRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {
        private final CustomerRepository customerRepository;
        private final OrderRepository orderRepository;

        public List<Customer> readCustomers() {
                return customerRepository.findAll();
        }

        public Customer createCustomer(Customer customer) {
                log.info("customer: {}", customer);
                String newCustomerId = generateNextCustomerId();
                customer.setCustomerId(newCustomerId);
                return customerRepository.save(customer);
        }

        private String generateNextCustomerId() {
                return "CUST-" + customerRepository.count();
        }

        @Transactional
        public void updateCustomer(String customerId, Customer customerDetails) {
                Customer existingCustomer = customerRepository.findById(customerId)
                                .orElseThrow(() -> new EntityNotFoundException("해당 ID의 거래처를 찾을 수 없습니다: " + customerId));

                existingCustomer.setCustomerName(customerDetails.getCustomerName());
                existingCustomer.setPhoneNumber(customerDetails.getPhoneNumber());
                existingCustomer.setEmail(customerDetails.getEmail());
                existingCustomer.setAddress(customerDetails.getAddress());

                customerRepository.save(existingCustomer);
        }

        @Transactional
        public void deleteCustomers(List<String> customerIds) {
                // customerRepository.deleteAllByCustomerIdIn(customerIds);
                for (String customerId : customerIds) {
                        if (orderRepository.existsByCustomerCustomerId(customerId)) {
                                throw new EntityExistsException(
                                                "해당 거래처가 사용된 주문서가 있어 삭제할 수 없습니다. (ID: " + customerId + ")");
                        }
                        // 주문서가 없으면 삭제 진행
                        customerRepository.deleteById(customerId);
                }
        }

}
