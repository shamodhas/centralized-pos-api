package com.oc.api.service;

import com.oc.api.constant.AppConstants;
import com.oc.api.model.master.TenantConfig;
import com.oc.api.repository.master.TenantConfigRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    @Qualifier(AppConstants.MASTER_DATASOURCE)
    private final DataSource masterDataSource;

    private final TenantConfigRepository tenantConfigRepository;

    public String registerTenant(String tenantName) {
        if (tenantName == null || tenantName.isBlank()) {
            throw new IllegalArgumentException("Tenant name cannot be null or blank");
        }

        String tenantId = "t_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String schemaName = "tenant_" + tenantId.toLowerCase();

        JdbcTemplate masterJdbcTemplate = new JdbcTemplate(masterDataSource);

        try {
            provisionSchemaAndTables(masterJdbcTemplate, schemaName);
            persistTenantConfig(masterJdbcTemplate, tenantId, tenantName, schemaName);

            return tenantId;
        } catch (Exception e) {
            rollbackTenantProvisioning(masterJdbcTemplate, schemaName);
            throw new RuntimeException("Failed to register tenant schema: " + e.getMessage(), e);
        }
    }

    public List<TenantConfig> getAllTenants() {
        return tenantConfigRepository.findAll();
    }

    public void updateTenantStatus(String tenantId, String status) {
        TenantConfig config = tenantConfigRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
        config.setStatus(status.toUpperCase());
        tenantConfigRepository.save(config);
    }

    private void provisionSchemaAndTables(JdbcTemplate jdbcTemplate, String schemaName) {
        if (!schemaName.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid schema format detected.");
        }

        jdbcTemplate.execute(String.format("CREATE SCHEMA \"%s\"", schemaName));

        LocalContainerEntityManagerFactoryBean emfBuilder = getLocalContainerEntityManagerFactoryBean(schemaName);

        try {
            emfBuilder.afterPropertiesSet();
            if (emfBuilder.getObject() != null) {
                emfBuilder.getObject().close();
            }
        } finally {
            emfBuilder.destroy();
        }
    }

    private @NonNull LocalContainerEntityManagerFactoryBean getLocalContainerEntityManagerFactoryBean(String schemaName) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.default_schema", schemaName);
        props.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        LocalContainerEntityManagerFactoryBean emfBuilder = new LocalContainerEntityManagerFactoryBean();
        emfBuilder.setDataSource(masterDataSource);
        emfBuilder.setPackagesToScan("com.oc.api.model.tenant");
        emfBuilder.setPersistenceProvider(new org.hibernate.jpa.HibernatePersistenceProvider());
        emfBuilder.setJpaPropertyMap(props);
        return emfBuilder;
    }

    private void persistTenantConfig(JdbcTemplate jdbcTemplate, String tenantId, String tenantName, String schemaName) {
        String insertSql = """
                    INSERT INTO tenant_config (tenant_id, tenant_name, schema_name, status, created_at) 
                    VALUES (?, ?, ?, 'ACTIVE', NOW())
                """;

        jdbcTemplate.update(insertSql, tenantId, tenantName, schemaName);
    }

    private void rollbackTenantProvisioning(JdbcTemplate jdbcTemplate, String schemaName) {
        try {
            jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS \"%s\" CASCADE", schemaName));
        } catch (Exception cleanupEx) {
            // Suppress secondary cleanup exceptions
        }
    }
}