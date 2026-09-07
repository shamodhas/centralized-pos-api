// src/main/java/com/oc/api/service/TenantManagementService.java
package com.oc.api.service;

import com.oc.api.security.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class TenantManagementService {

    @Autowired
    @Qualifier("masterDataSourceBean")
    private DataSource masterDataSource;

    @Autowired
    private EncryptionService encryptionService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional(transactionManager = "transactionManager")
    public String registerTenant(String tenantName) {
        try {
            if (tenantName == null || tenantName.isBlank()) return null;

            String tenantId = "t_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            String dbUsername = tenantId + "_user";
            String rawPassword = generateSecurePassword();

            JdbcTemplate masterJdbcTemplate = new JdbcTemplate(masterDataSource);

            // 1. Create dedicated role
            masterJdbcTemplate.execute("CREATE ROLE " + dbUsername + " WITH LOGIN PASSWORD '" + rawPassword + "'");

            // 2. CRITICAL: Grant database connection permission to the new user
            masterJdbcTemplate.execute("GRANT CONNECT ON DATABASE master_db TO " + dbUsername);

            // 3. Create schema and assign ownership
            masterJdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS \"" + tenantId + "\" AUTHORIZATION " + dbUsername);
            masterJdbcTemplate.execute("REVOKE ALL ON SCHEMA public FROM " + dbUsername);
            masterJdbcTemplate.execute("GRANT USAGE, CREATE ON SCHEMA \"" + tenantId + "\" TO " + dbUsername);

            // 4. Create the product table in the new schema
            masterJdbcTemplate.execute("CREATE TABLE \"" + tenantId + "\".product (id BIGSERIAL PRIMARY KEY, name VARCHAR(255), price DOUBLE PRECISION)");
            masterJdbcTemplate.execute("ALTER TABLE \"" + tenantId + "\".product OWNER TO " + dbUsername);

            // 5. Register routing configuration
            String tenantDbUrl = "jdbc:postgresql://localhost:5432/master_db?currentSchema=" + tenantId;
            String encryptedPassword = encryptionService.encrypt(rawPassword);

            String insertSql = """
                INSERT INTO tenant_config (tenant_id, db_url, db_username, db_password, db_driver) 
                VALUES (?, ?, ?, ?, ?) 
            """;

            masterJdbcTemplate.update(insertSql, tenantId, tenantDbUrl, dbUsername, encryptedPassword, "org.postgresql.Driver");

            return tenantId;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String generateSecurePassword() {
        byte[] randomBytes = new byte[24];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}