package com.oc.api.config;

import com.oc.api.constant.AppConstants;
import com.oc.api.context.DynamicRoutingDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableJpaAuditing
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.oc.api.repository.tenant",
        entityManagerFactoryRef = AppConstants.TENANT_ENTITY_MANAGER,
        transactionManagerRef = AppConstants.TENANT_TX_MANAGER
)
public class TenantJpaConfig {

    @Bean(name = AppConstants.ROUTING_DATASOURCE)
    public DynamicRoutingDataSource routingDataSource(
            TenantDataSourceManager tenantDataSourceManager,
            @Qualifier(AppConstants.MASTER_DATASOURCE) DataSource masterDataSource) {

        DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource(tenantDataSourceManager);
        routingDataSource.setTargetDataSources(Map.of(AppConstants.MASTER_TENANT_ID, masterDataSource));
        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        routingDataSource.afterPropertiesSet();

        tenantDataSourceManager.setRoutingDataSource(routingDataSource);
        return routingDataSource;
    }

    @Bean(name = AppConstants.TENANT_ENTITY_MANAGER)
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory(
            @Qualifier(AppConstants.ROUTING_DATASOURCE) DynamicRoutingDataSource routingDataSource) {

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(routingDataSource);
        factory.setPackagesToScan("com.oc.api.model.tenant");
        factory.setPersistenceUnitName("tenantPU");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
                "hibernate.connection.release_mode", "after_statement",
                "hibernate.hbm2ddl.auto", "none",
                "hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
                "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"
        ));

        return factory;
    }

    @Bean(name = AppConstants.TENANT_TX_MANAGER)
    public PlatformTransactionManager tenantTransactionManager(
            @Qualifier(AppConstants.TENANT_ENTITY_MANAGER) EntityManagerFactory tenantEntityManagerFactory) {
        return new JpaTransactionManager(tenantEntityManagerFactory);
    }

    @Bean(name = AppConstants.TENANT_TX_TEMPLATE)
    public TransactionTemplate tenantTransactionTemplate(
            @Qualifier(AppConstants.TENANT_TX_MANAGER) PlatformTransactionManager tenantTransactionManager) {
        return new TransactionTemplate(tenantTransactionManager);
    }
}