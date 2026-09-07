package com.oc.api.controller;

import com.oc.api.constant.AppConstants;
import com.oc.api.context.TenantContext;
import com.oc.api.dto.AuthRequest;
import com.oc.api.dto.AuthResponse;
import com.oc.api.dto.RefreshTokenRequest;
import com.oc.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> tenantLogin(
            @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.tenantLogin(request));
    }

    @PostMapping("/admin-login")
    public ResponseEntity<AuthResponse> adminLogin(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.adminLogin(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

}