package com.oc.api.controller;

import com.oc.api.dto.TenantRegistrationRequest;
import com.oc.api.config.tenant.TenantDataSourceManager;
import com.oc.api.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantManagementService;

    @Autowired
    private TenantDataSourceManager tenantDataSourceManager;

    @PostMapping("/register")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> registerTenant(@RequestBody TenantRegistrationRequest request) {
        String tenantId = tenantManagementService.registerTenant(request.getTenantName());

        if (tenantId != null) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Tenant registered and provisioned successfully",
                    "tenantId", tenantId
            ));
        } else {
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", "Tenant registration failed"));
        }
    }

    @DeleteMapping("/{tenantId}/cache")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> evictTenantCache(@PathVariable String tenantId) {
        tenantDataSourceManager.evictDataSource(tenantId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Tenant cache and connection pool evicted for: " + tenantId
        ));
    }
}