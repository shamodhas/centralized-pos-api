package com.oc.api.config;

import com.oc.api.constant.AppConstants;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.oc.api.repository.master",
        entityManagerFactoryRef = AppConstants.MASTER_ENTITY_MANAGER,
        transactionManagerRef = AppConstants.MASTER_TX_MANAGER
)
public class MasterJpaConfig {

    @Primary
    @Bean(name = AppConstants.MASTER_DATASOURCE)
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = AppConstants.MASTER_ENTITY_MANAGER)
    public LocalContainerEntityManagerFactoryBean masterEntityManagerFactory(
            @Qualifier(AppConstants.MASTER_DATASOURCE) DataSource masterDataSource) {

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(masterDataSource);
        factory.setPackagesToScan("com.oc.api.model.master");
        factory.setPersistenceUnitName("masterPU");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of(
                "hibernate.hbm2ddl.auto", "update",
                "hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy",
                "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"
        ));

        return factory;
    }

    @Primary
    @Bean(name = AppConstants.MASTER_TX_MANAGER)
    public PlatformTransactionManager masterTransactionManager(
            @Qualifier(AppConstants.MASTER_ENTITY_MANAGER) EntityManagerFactory masterEntityManagerFactory) {
        return new JpaTransactionManager(masterEntityManagerFactory);
    }

    @Primary
    @Bean(name = AppConstants.MASTER_TX_TEMPLATE)
    public TransactionTemplate masterTransactionTemplate(
            @Qualifier(AppConstants.MASTER_TX_MANAGER) PlatformTransactionManager masterTransactionManager) {
        return new TransactionTemplate(masterTransactionManager);
    }
}