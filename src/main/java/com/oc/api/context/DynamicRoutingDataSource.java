package com.oc.api.context;

import com.oc.api.manager.TenantDataSourceManager;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Map;

public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    private final TenantDataSourceManager tenantDataSourceManager;

    public DynamicRoutingDataSource(TenantDataSourceManager tenantDataSourceManager) {
        this.tenantDataSourceManager = tenantDataSourceManager;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String tenantId = TenantContext.getCurrentTenant();
        return (tenantId == null || tenantId.isBlank()) ? "master" : tenantId;
    }

    @Override
    protected DataSource determineTargetDataSource() {
        Object lookupKey = determineCurrentLookupKey();
        if ("master".equals(lookupKey)) {
            return super.determineTargetDataSource();
        }
        return tenantDataSourceManager.getDataSource((String) lookupKey);
    }

    public synchronized void updateTargetDataSources(Map<Object, Object> targetDataSources) {
        setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();

        try {
            Field resolvedDataSourcesField = AbstractRoutingDataSource.class.getDeclaredField("resolvedDataSources");
            resolvedDataSourcesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<Object, DataSource> resolvedDataSources = (Map<Object, DataSource>) resolvedDataSourcesField.get(this);

            for (Map.Entry<Object, Object> entry : targetDataSources.entrySet()) {
                if (entry.getValue() instanceof DataSource) {
                    resolvedDataSources.put(entry.getKey(), (DataSource) entry.getValue());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update resolved data sources", e);
        }
    }
}