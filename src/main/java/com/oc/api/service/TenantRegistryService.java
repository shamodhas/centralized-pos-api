package com.oc.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TenantRegistryService {

    private static final String TENANT_CACHE_PREFIX = "tenant:route:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    public void cacheTenantRoute(String tenantId, String dbUrl) {
        redisTemplate.opsForValue().set(TENANT_CACHE_PREFIX + tenantId, dbUrl);
    }

    public String getCachedTenantRoute(String tenantId) {
        return redisTemplate.opsForValue().get(TENANT_CACHE_PREFIX + tenantId);
    }

    public void evictTenantRoute(String tenantId) {
        redisTemplate.delete(TENANT_CACHE_PREFIX + tenantId);
    }
}