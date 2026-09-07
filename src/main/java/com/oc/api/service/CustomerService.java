package com.oc.api.service;

import com.oc.api.model.tenant.Customer;
import com.oc.api.repository.tenant.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public Customer registerCustomer(String name, String phone, String email) {
        if (customerRepository.findByPhone(phone).isPresent()) {
            throw new IllegalArgumentException("Customer already exists with phone: " + phone);
        }

        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setLoyaltyPoints(0);

        return customerRepository.save(customer);
    }

    @Transactional
    public void addLoyaltyPoints(String phone, BigDecimal totalAmount) {
        customerRepository.findByPhone(phone).ifPresent(customer -> {
            int earnedPoints = totalAmount.divide(BigDecimal.valueOf(100), java.math.RoundingMode.DOWN).intValue();
            customer.setLoyaltyPoints(customer.getLoyaltyPoints() + earnedPoints);
            customerRepository.save(customer);
        });
    }
}