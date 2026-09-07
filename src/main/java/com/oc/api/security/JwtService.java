package com.oc.api.security;

import com.oc.api.constant.AppConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.refresh-secret}")
    private String refreshSecretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationTime;

    private Key getSignKey(boolean isRefresh) {
        String key = isRefresh ? refreshSecretKey : secretKey;
        return Keys.hmacShaKeyFor(key.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject, false);
    }

    public String extractUsernameFromRefresh(String token) {
        return extractClaim(token, Claims::getSubject, true);
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
        return buildToken(claims, userDetails.getUsername(), expirationTime, false);
    }

    public String generateRefreshToken(UserDetails userDetails, String tenantId, String userType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(AppConstants.CLAIM_TENANT_ID, tenantId);
        claims.put(AppConstants.CLAIM_USER_TYPE, userType);
        return buildToken(claims, userDetails.getUsername(), refreshExpirationTime, true);
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