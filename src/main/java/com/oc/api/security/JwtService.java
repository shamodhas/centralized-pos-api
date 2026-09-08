package com.oc.api.security;

import com.oc.api.config.SecurityProperties;
import com.oc.api.constant.AppConstants;
import com.oc.api.exception.types.InvalidTokenException;
import com.oc.api.exception.types.TokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
public class JwtService {

    private final SecurityProperties properties;

    private Key getSignKey(boolean isRefresh) {
        String key = isRefresh ? properties.refreshSecret() : properties.secret();
        return Keys.hmacShaKeyFor(key.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject, false);
    }

    public String extractUsernameFromRefresh(String token) {
        return extractClaim(token, Claims::getSubject, true);
    }

    public String extractTenantId(String token) {
        return extractClaim(token, claims -> claims.get(AppConstants.CLAIM_TENANT_ID, String.class), false);
    }

    public String extractUserType(String token) {
        return extractClaim(token, claims -> claims.get(AppConstants.CLAIM_USER_TYPE, String.class), false);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, String>> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get(AppConstants.CLAIM_ROLES, List.class), false);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, boolean isRefresh) {
        final Claims claims = extractAllClaims(token, isRefresh);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails, String tenantId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(AppConstants.CLAIM_ROLES, userDetails.getAuthorities());
        claims.put(AppConstants.CLAIM_TENANT_ID, tenantId);
        claims.put(AppConstants.CLAIM_USER_TYPE, userType);
        return buildToken(claims, userDetails.getUsername(), properties.expiration(), false);
    }

    public String generateRefreshToken(UserDetails userDetails, String tenantId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(AppConstants.CLAIM_TENANT_ID, tenantId);
        claims.put(AppConstants.CLAIM_USER_TYPE, userType);
        return buildToken(claims, userDetails.getUsername(), properties.refreshExpiration(), true);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration, boolean isRefresh) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignKey(isRefresh), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, false);
    }

    public boolean isTokenValidWithoutUser(String token) {
        try {
            return !isTokenExpired(token, false);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new TokenExpiredException("JWT token has expired: " + e.getMessage());
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid JWT token: " + e.getMessage());
        }
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsernameFromRefresh(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, true);
    }

    private boolean isTokenExpired(String token, boolean isRefresh) {
        return extractExpiration(token, isRefresh).before(new Date());
    }

    private Date extractExpiration(String token, boolean isRefresh) {
        return extractClaim(token, Claims::getExpiration, isRefresh);
    }

    private Claims extractAllClaims(String token, boolean isRefresh) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey(isRefresh))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}