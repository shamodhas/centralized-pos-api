package com.oc.api.controller;

import com.oc.api.model.tenant.User;
import com.oc.api.repository.tenant.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/users")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody User request, @RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        request.setPassword(passwordEncoder.encode(request.getPassword()));

        if (tenantId != null && !tenantId.isBlank()) {
            request.setTenantId(tenantId);
        }

        User savedUser = userRepository.save(request);
        return ResponseEntity.ok(savedUser);
    }
}