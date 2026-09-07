package com.oc.api.repository.master;

import com.oc.api.model.master.TenantConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantConfigRepository extends JpaRepository<TenantConfig, Long> {
    Optional<TenantConfig> findByTenantId(String tenantId);
    boolean existsByTenantName(String tenantName);
}