package com.oc.api.config.base;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * ========================================================
 * Author  : Shamodha Sahan
 * GitHub  : https://github.com/shamodhas
 * Website : https://shamodha.com
 * ========================================================
 * Date    : 9/8/2026 2:50 PM
 * Project : api
 * ========================================================
 */

public abstract class BaseJpaConfig {
    
    protected LocalContainerEntityManagerFactoryBean createEntityManagerFactory(
            DataSource dataSource,
            String[] packagesToScan,
            String persistenceUnitName,
            JpaVendorAdapter vendorAdapter,
            Map<String, Object> properties
    ) {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan(packagesToScan);
        factory.setPersistenceUnitName(persistenceUnitName);
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setJpaPropertyMap(properties);

        return factory;
    }

    protected PlatformTransactionManager createTransactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    protected TransactionTemplate createTransactionTemplate(PlatformTransactionManager ptm) {
        return new TransactionTemplate(ptm);
    }
}
