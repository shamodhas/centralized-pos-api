package com.oc.api.config;

import com.oc.api.config.base.BaseJpaConfig;
import com.oc.api.constant.AppConstants;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Map;

/**
 * ========================================================
 * Author  : Shamodha Sahan
 * GitHub  : https://github.com/shamodhas
 * Website : https://shamodha.com
 * ========================================================
 * Date    : 9/8/2026 3:05 PM
 * Project : api
 * ========================================================
 */

@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.oc.api.repository.master",
        entityManagerFactoryRef = AppConstants.MASTER_ENTITY_MANAGER,
        transactionManagerRef = AppConstants.MASTER_TX_MANAGER
)
public class MasterJpaConfig extends BaseJpaConfig {

    @Primary
    @Bean(name = AppConstants.MASTER_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = AppConstants.MASTER_ENTITY_MANAGER)
    public LocalContainerEntityManagerFactoryBean masterEntityManagerFactory(
            @Qualifier(AppConstants.MASTER_DATASOURCE) DataSource dataSource
    ) {
        return createEntityManagerFactory(
                dataSource,
                new String[]{"com.oc.api.model.master"},
                "masterPU",
                new HibernateJpaVendorAdapter(),
                Map.of(
                        "hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
                        "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
                        "hibernate.hbm2ddl.auto", "update"
                )
        );
    }

    @Primary
    @Bean(name = AppConstants.MASTER_TX_MANAGER)
    public PlatformTransactionManager masterTransactionManager(
            @Qualifier(AppConstants.MASTER_ENTITY_MANAGER) EntityManagerFactory entityManagerFactory
    ) {
        return createTransactionManager(entityManagerFactory);
    }

    @Primary
    @Bean(name = AppConstants.MASTER_TX_TEMPLATE)
    public TransactionTemplate masterTransactionTemplate(
            @Qualifier(AppConstants.MASTER_TX_MANAGER) PlatformTransactionManager platformTransactionManager
    ) {
        return createTransactionTemplate(platformTransactionManager);
    }
}