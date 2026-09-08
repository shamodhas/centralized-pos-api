package com.oc.api.config;

import com.oc.api.constant.AppConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
public class SuperAdminSeeder {

    @Bean
    CommandLineRunner seedSuperAdmin(
            @Qualifier(AppConstants.MASTER_DATASOURCE) DataSource masterDataSource,
            PasswordEncoder passwordEncoder
    ) {
        JdbcTemplate masterJdbcTemplate = new JdbcTemplate(masterDataSource);

        return args -> {
            String checkSql = "SELECT COUNT(*) FROM global_admins WHERE username = ?";
            Integer count = masterJdbcTemplate.queryForObject(checkSql, Integer.class, "admin");

            if (count != null && count == 0) {
                String hashedPassword = passwordEncoder.encode("123456");
                String insertSql = "INSERT INTO global_admins (username, password, role) VALUES (?, ?, ?)";
                masterJdbcTemplate.update(insertSql, "admin", hashedPassword, "SUPER_ADMIN");
            }
        };
    }
}