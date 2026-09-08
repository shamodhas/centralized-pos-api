package com.oc.api.config.tenant;

import com.oc.api.constant.AppConstants;
import com.oc.api.context.DynamicRoutingDataSource;
import com.oc.api.model.master.TenantConfig;
import com.oc.api.repository.master.TenantConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
public class TenantDataSourceManager {

    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReentrantLock> tenantLocks = new ConcurrentHashMap<>();

    private final TenantConfigRepository tenantConfigRepository;

    @Qualifier(AppConstants.MASTER_DATASOURCE)
    private final DataSource masterDataSource;

    @Setter
    private DynamicRoutingDataSource routingDataSource;

    public DataSource getDataSource(String tenantId) {
        if (AppConstants.MASTER_TENANT_ID.equals(tenantId)) {
            return masterDataSource;
        }

        DataSource dataSource = tenantDataSources.get(tenantId);
        if (dataSource != null) {
            return dataSource;
        }

        ReentrantLock tenantLock = tenantLocks.computeIfAbsent(tenantId, k -> new ReentrantLock());
        tenantLock.lock();
        try {
            return tenantDataSources.computeIfAbsent(tenantId, this::createTenantSchemaDataSource);
        } finally {
            tenantLock.unlock();
        }
    }

    @Cacheable(value = "tenantConfigs", key = "#tenantId")
    public TenantConfig getTenantConfig(String tenantId) {
        return tenantConfigRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalStateException("Tenant configuration not found for ID: " + tenantId));
    }

    private DataSource createTenantSchemaDataSource(String tenantId) {
        TenantConfig config = getTenantConfig(tenantId);
        String schemaName = config.getSchemaName();

        DataSource schemaDataSource = new DelegatingDataSource(masterDataSource) {
            @Override
            public Connection getConnection() throws SQLException {
                Connection connection = super.getConnection();
                try (var statement = connection.createStatement()) {
                    statement.execute("SET search_path TO \"" + schemaName + "\"");
                } catch (SQLException e) {
                    connection.close();
                    throw e;
                }
                return connection;
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                Connection connection = super.getConnection(username, password);
                try (var statement = connection.createStatement()) {
                    statement.execute("SET search_path TO \"" + schemaName + "\"");
                } catch (SQLException e) {
                    connection.close();
                    throw e;
                }
                return connection;
            }
        };

        updateRoutingDataSourceTargets();
        return schemaDataSource;
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
        ReentrantLock tenantLock = tenantLocks.get(tenantId);
        if (tenantLock != null) {
            tenantLock.lock();
            try {
                tenantDataSources.remove(tenantId);
                updateRoutingDataSourceTargets();
            } finally {
                tenantLock.unlock();
                tenantLocks.remove(tenantId);
            }
        }
    }
}