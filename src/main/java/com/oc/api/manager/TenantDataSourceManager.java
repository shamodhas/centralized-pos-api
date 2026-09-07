package com.oc.api.manager;

import com.oc.api.context.DynamicRoutingDataSource;
import com.oc.api.security.EncryptionService;
import com.oc.api.service.TenantRegistryService;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class TenantDataSourceManager {

    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> tenantLocks = new ConcurrentHashMap<>();

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private TenantRegistryService tenantRegistryService;

    @Autowired
    @Qualifier("masterDataSourceBean")
    private DataSource masterDataSource;

    private DynamicRoutingDataSource routingDataSource;

    public void setRoutingDataSource(DynamicRoutingDataSource routingDataSource) {
        this.routingDataSource = routingDataSource;
    }

    public DataSource getDataSource(String tenantId) {
        if (tenantDataSources.containsKey(tenantId)) return tenantDataSources.get(tenantId);

        ReentrantLock tenantLock = tenantLocks.computeIfAbsent(tenantId, k -> new ReentrantLock());
        tenantLock.lock();
        try {
            if (tenantDataSources.containsKey(tenantId)) return tenantDataSources.get(tenantId);

            DataSource dataSource = createTenantDataSource(tenantId);
            tenantDataSources.put(tenantId, dataSource);
            updateRoutingDataSourceTargets();
            return dataSource;
        } finally {
            tenantLock.unlock();
            tenantLocks.remove(tenantId);
        }
    }

    private DataSource createTenantDataSource(String tenantId) {
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(masterDataSource);
            String sql = "SELECT db_url, db_username, db_password, db_driver FROM tenant_config WHERE tenant_id = ?";
            Map<String, Object> config = jdbcTemplate.queryForMap(sql, tenantId);

            HikariDataSource ds = new HikariDataSource();
            ds.setJdbcUrl((String) config.get("db_url"));
            ds.setUsername((String) config.get("db_username"));
            ds.setPassword(encryptionService.decrypt((String) config.get("db_password")));
            ds.setDriverClassName((String) config.get("db_driver"));
            ds.setMaximumPoolSize(10);

            tenantRegistryService.cacheTenantRoute(tenantId, ds.getJdbcUrl());
            return ds;
        } catch (Exception e) {
            throw new IllegalStateException("Failed configuring datasource for tenant: " + tenantId, e);
        }
    }

    private synchronized void updateRoutingDataSourceTargets() {
        if (routingDataSource != null) {
            Map<Object, Object> targetDataSources = new HashMap<>(tenantDataSources);
            targetDataSources.put("master", masterDataSource);
            routingDataSource.updateTargetDataSources(targetDataSources);
        }
    }

    public void evictDataSource(String tenantId) {
        ReentrantLock tenantLock = tenantLocks.computeIfAbsent(tenantId, k -> new ReentrantLock());
        tenantLock.lock();
        try {
            DataSource ds = tenantDataSources.remove(tenantId);
            if (ds instanceof HikariDataSource) {
                ((HikariDataSource) ds).close();
            }
            tenantRegistryService.evictTenantRoute(tenantId);
            updateRoutingDataSourceTargets();
        } finally {
            tenantLock.unlock();
            tenantLocks.remove(tenantId);
        }
    }
}