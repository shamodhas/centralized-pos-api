package com.oc.api.controller;

import com.oc.api.model.master.TenantConfig;
import com.oc.api.service.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tenants")
@RequiredArgsConstructor
public class GlobalAdminController {

    private final TenantService tenantManagementService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TenantConfig>> getAllTenants() {
        return ResponseEntity.ok(tenantManagementService.getAllTenants());
    }

    @PatchMapping("/{tenantId}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> updateTenantStatus(@PathVariable String tenantId, @RequestParam String status) {
        tenantManagementService.updateTenantStatus(tenantId, status);
        return ResponseEntity.ok("Tenant status updated successfully to " + status);
    }
}