package com.oc.api.service;

import com.oc.api.constant.AppConstants;
import com.oc.api.context.TenantContext;
import com.oc.api.dto.AuthRequest;
import com.oc.api.dto.AuthResponse;
import com.oc.api.dto.RefreshTokenRequest;
import com.oc.api.model.master.GlobalAdmin;
import com.oc.api.model.master.TenantUserMapping;
import com.oc.api.model.tenant.User;
import com.oc.api.repository.master.GlobalAdminRepository;
import com.oc.api.repository.master.TenantUserMappingRepository;
import com.oc.api.repository.tenant.UserRepository;
import com.oc.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GlobalAdminRepository globalAdminRepository;
    private final UserRepository userRepository;
    private final TenantUserMappingRepository tenantUserMappingRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthResponse tenantLogin(AuthRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required for tenant login");
        }

        TenantContext.setCurrentTenant(AppConstants.MASTER_TENANT_ID);
        String targetTenantId;
        try {
            TenantUserMapping mapping = tenantUserMappingRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User mapping not found in master database"));
            targetTenantId = mapping.getTenantId();
        } finally {
            TenantContext.clear();
        }

        TenantContext.setCurrentTenant(targetTenantId);
        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found in tenant schema"));

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Invalid password");
            }

            List<SimpleGrantedAuthority> authorities = user.getRoles() == null ? List.of() : user.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority(AppConstants.ROLE_PREFIX + r.name()))
                    .toList();

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .authorities((Collection) authorities)
                    .build();

            String accessToken = jwtService.generateToken(userDetails, targetTenantId, AppConstants.USER_TYPE_TENANT);
            String refreshToken = jwtService.generateRefreshToken(userDetails, targetTenantId, AppConstants.USER_TYPE_TENANT);

            Set<String> roleNames = user.getRoles() == null ? Set.of() : user.getRoles().stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.toSet());

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .email(user.getEmail())
                    .tenantId(targetTenantId)
                    .userType(AppConstants.USER_TYPE_TENANT)
                    .roles(roleNames)
                    .build();
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse adminLogin(AuthRequest request) {
        TenantContext.setCurrentTenant(AppConstants.MASTER_TENANT_ID);
        try {
            GlobalAdmin admin = globalAdminRepository.findByUsername(request.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Global admin not found"));

            if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
                throw new BadCredentialsException("Invalid password");
            }

            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(AppConstants.ROLE_PREFIX + admin.getRole()));

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(admin.getUsername())
                    .password(admin.getPassword())
                    .authorities((Collection) authorities)
                    .build();

            String accessToken = jwtService.generateToken(userDetails, AppConstants.MASTER_TENANT_ID, AppConstants.USER_TYPE_GLOBAL);
            String refreshToken = jwtService.generateRefreshToken(userDetails, AppConstants.MASTER_TENANT_ID, AppConstants.USER_TYPE_GLOBAL);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .email(admin.getUsername())
                    .tenantId(AppConstants.MASTER_TENANT_ID)
                    .userType(AppConstants.USER_TYPE_GLOBAL)
                    .roles(Set.of(admin.getRole()))
                    .build();
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        String email = jwtService.extractUsernameFromRefresh(token);
        String userType = jwtService.extractClaim(token, claims -> claims.get(AppConstants.CLAIM_USER_TYPE, String.class), true);
        String tenantId = jwtService.extractClaim(token, claims -> claims.get(AppConstants.CLAIM_TENANT_ID, String.class), true);

        if (email == null || userType == null) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        UserDetails userDetails;
        Set<String> roleNames = new HashSet<>();
        String resolvedTenant = tenantId != null ? tenantId : AppConstants.MASTER_TENANT_ID;

        if (AppConstants.USER_TYPE_GLOBAL.equals(userType)) {
            TenantContext.setCurrentTenant(AppConstants.MASTER_TENANT_ID);
            try {
                GlobalAdmin admin = globalAdminRepository.findByUsername(email)
                        .orElseThrow(() -> new UsernameNotFoundException("Global admin not found"));

                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(AppConstants.ROLE_PREFIX + admin.getRole()));

                userDetails = org.springframework.security.core.userdetails.User.builder()
                        .username(admin.getUsername())
                        .password(admin.getPassword())
                        .authorities((Collection) authorities)
                        .build();

                roleNames.add(admin.getRole());
            } finally {
                TenantContext.clear();
            }
        } else {
            if (tenantId == null) {
                throw new BadCredentialsException("Tenant ID missing in refresh token");
            }
            TenantContext.setCurrentTenant(tenantId);
            try {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found in tenant schema"));

                List<SimpleGrantedAuthority> authorities = user.getRoles() == null ? List.of() : user.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(AppConstants.ROLE_PREFIX + r.name()))
                        .toList();

                userDetails = org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password(user.getPassword())
                        .authorities((Collection) authorities)
                        .build();

                if (user.getRoles() != null) {
                    roleNames = user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
                }
            } finally {
                TenantContext.clear();
            }
        }

        if (!jwtService.isRefreshTokenValid(token, userDetails)) {
            throw new BadCredentialsException("Refresh token is expired or invalid");
        }

        String newAccessToken = jwtService.generateToken(userDetails, resolvedTenant, userType);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails, resolvedTenant, userType);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .email(email)
                .tenantId(resolvedTenant)
                .userType(userType)
                .roles(roleNames)
                .build();
    }
}