package com.oc.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.tenant.db")
public class TenantProperties {
    private String host = "localhost";
    private int port = 5432;
    private String driverClassName = "org.postgresql.Driver";
    private Pool pool = new Pool();

    @Getter
    @Setter
    public static class Pool {
        private int maximumPoolSize = 10;
        private int minimumIdle = 2;
        private long connectionTimeout = 30000;
    }
}