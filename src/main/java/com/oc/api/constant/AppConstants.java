package com.oc.api.constant;

public final class AppConstants {

    private AppConstants() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static final String USER_TYPE_GLOBAL = "GLOBAL";
    public static final String USER_TYPE_TENANT = "TENANT";

    public static final String MASTER_TENANT_ID = "master";

    public static final String CLAIM_USER_TYPE = "userType";
    public static final String CLAIM_TENANT_ID = "tenantId";
    public static final String CLAIM_ROLES = "roles";

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_TENANT_ID = "X-TenantID";
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String ROLE_PREFIX = "ROLE_";

    public static final String MASTER_DATASOURCE = "masterDataSourceBean";
    public static final String ROUTING_DATASOURCE = "routingDataSource";

    public static final String TENANT_CACHE_PREFIX = "tenant:route:";

    public static final String MASTER_TX_MANAGER = "masterTransactionManager";
    public static final String MASTER_TX_TEMPLATE = "masterTransactionTemplate";
    public static final String MASTER_ENTITY_MANAGER = "masterEntityManagerFactory";

    public static final String TENANT_TX_MANAGER = "tenantTransactionManager";
    public static final String TENANT_TX_TEMPLATE = "tenantTransactionTemplate";
    public static final String TENANT_ENTITY_MANAGER = "tenantEntityManagerFactory";

    public static final String[] PUBLIC_URLS = {
            "/api/auth/**"
    };
}