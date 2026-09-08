package com.oc.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record SecurityProperties(
        String secret,
        String refreshSecret,
        long expiration,
        long refreshExpiration
) {
}