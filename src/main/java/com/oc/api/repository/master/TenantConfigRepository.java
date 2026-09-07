package com.oc.api.repository.master;

import com.oc.api.model.master.TenantConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantConfigRepository extends JpaRepository<TenantConfig, String> {
}