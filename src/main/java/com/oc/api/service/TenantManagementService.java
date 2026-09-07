package com.oc.api.service;

import com.oc.api.config.TenantProperties;
import com.oc.api.constant.AppConstants;
import com.oc.api.security.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantManagementService {

    @Qualifier(AppConstants.MASTER_DATASOURCE)
    private final DataSource masterDataSource;

    private final EncryptionService encryptionService;
    private final TenantProperties tenantProperties;

    public String registerTenant(String tenantName) {
        if (tenantName == null || tenantName.isBlank()) {
            throw new IllegalArgumentException("Tenant name cannot be null or blank");
        }

        String tenantId = "t_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String dbName = "tenant_" + tenantId.toLowerCase();
        String dbUsername = tenantId + "_user";
        String rawPassword = encryptionService.generateSecurePassword();
        String tenantDbUrl = String.format("jdbc:postgresql://%s:%d/%s", tenantProperties.getHost(), tenantProperties.getPort(), dbName);

        JdbcTemplate masterJdbcTemplate = new JdbcTemplate(masterDataSource);

        try {
            provisionDatabaseAndRole(masterJdbcTemplate, dbName, dbUsername, rawPassword);
            initializeTenantSchema(tenantDbUrl, dbUsername, rawPassword);
            persistTenantConfig(masterJdbcTemplate, tenantId, tenantName, tenantDbUrl, dbUsername, rawPassword);

            return tenantId;
        } catch (Exception e) {
            rollbackTenantProvisioning(masterJdbcTemplate, dbName, dbUsername);
            throw new RuntimeException("Failed to register tenant database: " + e.getMessage(), e);
        }
    }

    private void provisionDatabaseAndRole(JdbcTemplate jdbcTemplate, String dbName, String dbUsername, String rawPassword) {
        if (!dbName.matches("^[a-zA-Z0-9_]+$") || !dbUsername.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid database or username format detected.");
        }

        jdbcTemplate.execute(String.format("CREATE DATABASE \"%s\"", dbName));
        jdbcTemplate.execute(String.format("CREATE ROLE \"%s\" WITH LOGIN PASSWORD '%s'", dbUsername, rawPassword.replace("'", "''")));
        jdbcTemplate.execute(String.format("GRANT ALL PRIVILEGES ON DATABASE \"%s\" TO \"%s\"", dbName, dbUsername));
        jdbcTemplate.execute(String.format("ALTER DATABASE \"%s\" OWNER TO \"%s\"", dbName, dbUsername));
    }

    private void initializeTenantSchema(String tenantDbUrl, String dbUsername, String rawPassword) {
        LocalContainerEntityManagerFactoryBean emfBuilder = buildEntityManagerFactory(tenantDbUrl, dbUsername, rawPassword);
        try {
            emfBuilder.afterPropertiesSet();
            if (emfBuilder.getObject() != null) {
                emfBuilder.getObject().close();
            }
        } finally {
            emfBuilder.destroy();
        }
    }

    private LocalContainerEntityManagerFactoryBean buildEntityManagerFactory(String tenantDbUrl, String dbUsername, String rawPassword) {
        DriverManagerDataSource freshTenantDataSource = new DriverManagerDataSource();
        freshTenantDataSource.setDriverClassName(tenantProperties.getDriverClassName());
        freshTenantDataSource.setUrl(tenantDbUrl);
        freshTenantDataSource.setUsername(dbUsername);
        freshTenantDataSource.setPassword(rawPassword);

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "update");
        props.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        LocalContainerEntityManagerFactoryBean emfBuilder = new LocalContainerEntityManagerFactoryBean();
        emfBuilder.setDataSource(freshTenantDataSource);
        emfBuilder.setPackagesToScan("com.oc.api.model.tenant");
        emfBuilder.setPersistenceProvider(new org.hibernate.jpa.HibernatePersistenceProvider());
        emfBuilder.setJpaPropertyMap(props);

        return emfBuilder;
    }

    private void persistTenantConfig(JdbcTemplate jdbcTemplate, String tenantId, String tenantName, String tenantDbUrl, String dbUsername, String rawPassword) throws Exception {
        String driver = tenantProperties.getDriverClassName();
        String encryptedPassword = encryptionService.encrypt(rawPassword);

        String insertSql = """
                    INSERT INTO tenant_config (tenant_id, tenant_name, db_url, db_username, db_password, db_driver) 
                    VALUES (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(insertSql, tenantId, tenantName, tenantDbUrl, dbUsername, encryptedPassword, driver);
    }

    private void rollbackTenantProvisioning(JdbcTemplate jdbcTemplate, String dbName, String dbUsername) {
        try {
            jdbcTemplate.execute(String.format("DROP DATABASE IF EXISTS \"%s\" WITH (FORCE)", dbName));
            jdbcTemplate.execute(String.format("DROP ROLE IF EXISTS \"%s\"", dbUsername));
        } catch (Exception cleanupEx) {
            // Suppress secondary cleanup exceptions
        }
    }
}