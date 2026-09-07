package com.oc.api.controller;

import com.oc.api.dto.UserRegistrationRequest;
import com.oc.api.security.CustomUserDetails;
import com.oc.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<String> registerUser(
            @RequestBody UserRegistrationRequest request,
            @RequestParam("tenantId") String targetTenantId) {

        userService.registerUser(request, targetTenantId);
        return ResponseEntity.ok("User registered successfully to tenant: " + targetTenantId);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.getCurrentUserProfile(userDetails));
    }
}