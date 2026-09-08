package com.oc.api.service;

import com.oc.api.constant.AppConstants;
import com.oc.api.context.TenantContext;
import com.oc.api.dto.AuthResponse;
import com.oc.api.dto.UserRegistrationRequest;
import com.oc.api.model.master.TenantUserMapping;
import com.oc.api.model.tenant.User;
import com.oc.api.repository.master.GlobalAdminRepository;
import com.oc.api.repository.master.TenantUserMappingRepository;
import com.oc.api.repository.tenant.UserRepository;
import com.oc.api.security.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GlobalAdminRepository globalAdminRepository;
    private final TenantUserMappingRepository tenantUserMappingRepository;
    private final PasswordEncoder passwordEncoder;

    @Qualifier(AppConstants.MASTER_TX_TEMPLATE)
    private final TransactionTemplate masterTransactionTemplate;

    @Qualifier(AppConstants.TENANT_TX_TEMPLATE)
    private final TransactionTemplate tenantTransactionTemplate;

    public void registerUser(UserRegistrationRequest request, String targetTenantId) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        masterTransactionTemplate.execute(masterStatus -> {
            TenantContext.setCurrentTenant(AppConstants.MASTER_TENANT_ID);
            try {
                if (tenantUserMappingRepository.existsByEmail(request.getEmail())) {
                    throw new IllegalStateException("User mapping already exists for email: " + request.getEmail());
                }

                TenantUserMapping mapping = TenantUserMapping.builder()
                        .email(request.getEmail())
                        .tenantId(targetTenantId)
                        .build();

                tenantUserMappingRepository.save(mapping);

                TenantContext.setCurrentTenant(targetTenantId);

                tenantTransactionTemplate.execute(tenantStatus -> {
                    try {
                        if (userRepository.existsByEmail(request.getEmail())) {
                            throw new IllegalStateException("User already exists in tenant schema: " + targetTenantId);
                        }

                        User user = new User();
                        user.setEmail(request.getEmail());
                        user.setPassword(passwordEncoder.encode(request.getPassword()));
                        user.setTenantId(targetTenantId);
                        user.setRoles(request.getRoles());

                        userRepository.save(user);
                        return null;
                    } finally {
                        TenantContext.clear();
                    }
                });

                return null;
            } finally {
                TenantContext.clear();
            }
        });
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUserProfile(UserPrincipal userDetails) {
        Long userId = userDetails.getId();
        String tenantId = userDetails.getTenantId();
        String userType = userDetails.getUserType();

        if (AppConstants.USER_TYPE_GLOBAL.equals(userType)) {
            TenantContext.setCurrentTenant(AppConstants.MASTER_TENANT_ID);
            try {
                globalAdminRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("Global admin not found"));
            } finally {
                TenantContext.clear();
            }
        } else {
            TenantContext.setCurrentTenant(tenantId);
            try {
                userRepository.findById(userId)
                        .orElseThrow(() -> new EntityNotFoundException("User not found in tenant schema"));
            } finally {
                TenantContext.clear();
            }
        }

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace(AppConstants.ROLE_PREFIX, ""))
                .collect(Collectors.toSet());

        return AuthResponse.builder()
                .id(userDetails.getId())
                .accessToken(null)
                .refreshToken(null)
                .tokenType("Bearer")
                .email(userDetails.getUsername())
                .tenantId(tenantId)
                .userType(userType)
                .roles(roles)
                .build();
    }
}