package com.oc.api.service;

import com.oc.api.constant.AppConstants;
import com.oc.api.context.DynamicRoutingDataSource;
import com.oc.api.model.master.TenantConfig;
import com.oc.api.repository.master.TenantConfigRepository;
import com.oc.api.security.EncryptionService;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
public class TenantDataSourceManager {

    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> tenantLocks = new ConcurrentHashMap<>();

    private final EncryptionService encryptionService;
    private final TenantConfigRepository tenantConfigRepository;

    @Qualifier(AppConstants.MASTER_DATASOURCE)
    private final DataSource masterDataSource;

    @Setter
    private DynamicRoutingDataSource routingDataSource;

    public DataSource getDataSource(String tenantId) {
        if (tenantDataSources.containsKey(tenantId)) {
            return tenantDataSources.get(tenantId);
        }

        ReentrantLock tenantLock = tenantLocks.computeIfAbsent(tenantId, k -> new ReentrantLock());
        tenantLock.lock();
        try {
            if (tenantDataSources.containsKey(tenantId)) {
                return tenantDataSources.get(tenantId);
            }

            DataSource dataSource = createTenantDataSource(tenantId);
            tenantDataSources.put(tenantId, dataSource);
            updateRoutingDataSourceTargets();
            return dataSource;
        } finally {
            tenantLock.unlock();
        }
    }

    @Cacheable(value = "tenantConfigs", key = "#tenantId")
    public TenantConfig getTenantConfig(String tenantId) {
        return tenantConfigRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant configuration not found for ID: " + tenantId));
    }

    private DataSource createTenantDataSource(String tenantId) {
        try {
            TenantConfig config = getTenantConfig(tenantId);

            String decryptedPassword = encryptionService.decrypt(config.getDbPassword());

            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl(config.getDbUrl());
            ds.setUsername(config.getDbUsername());
            ds.setPassword(decryptedPassword);
            ds.setDriverClassName(config.getDbDriver());
            ds.setMaximumPoolSize(10);

            return ds;
        } catch (Exception e) {
            throw new IllegalStateException("Failed configuring datasource for tenant: " + tenantId + " -> Cause: " + e.getMessage(), e);
        }
    }

    private synchronized void updateRoutingDataSourceTargets() {
        if (routingDataSource != null) {
            Map<Object, Object> targetDataSources = new HashMap<>(tenantDataSources);
            targetDataSources.put(AppConstants.MASTER_TENANT_ID, masterDataSource);
            routingDataSource.updateTargetDataSources(targetDataSources);
        }
    }

    @CacheEvict(value = "tenantConfigs", key = "#tenantId")
    public void evictDataSource(String tenantId) {
        ReentrantLock tenantLock = tenantLocks.computeIfAbsent(tenantId, k -> new ReentrantLock());
        tenantLock.lock();
        try {
            DataSource ds = tenantDataSources.remove(tenantId);
            if (ds instanceof HikariDataSource) {
                ((HikariDataSource) ds).close();
            }
            updateRoutingDataSourceTargets();
        } finally {
            tenantLock.unlock();
            tenantLocks.remove(tenantId);
        }
    }
}