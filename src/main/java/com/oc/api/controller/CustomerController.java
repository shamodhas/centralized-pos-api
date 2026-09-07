package com.oc.api.controller;

import com.oc.api.model.tenant.Customer;
import com.oc.api.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<Customer> registerCustomer(
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(customerService.registerCustomer(name, phone, email));
    }
}