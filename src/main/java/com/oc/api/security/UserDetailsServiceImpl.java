package com.oc.api.security;

import com.oc.api.constant.AppConstants;
import com.oc.api.context.TenantContext;
import com.oc.api.model.master.GlobalAdmin;
import com.oc.api.model.tenant.User;
import com.oc.api.repository.master.GlobalAdminRepository;
import com.oc.api.repository.tenant.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final GlobalAdminRepository globalAdminRepository;
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String currentTenant = TenantContext.getCurrentTenant();

        // Handle Global Admin vs Tenant User lookup based on context
        if (AppConstants.MASTER_TENANT_ID.equals(currentTenant) || currentTenant == null) {
            GlobalAdmin admin = globalAdminRepository.findByUsername(email).orElse(null);
            if (admin != null) {
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(AppConstants.ROLE_PREFIX + admin.getRole()));
                return new CustomUserDetails(
                        admin.getId(),
                        admin.getUsername(),
                        null,
                        AppConstants.MASTER_TENANT_ID,
                        AppConstants.USER_TYPE_GLOBAL,
                        authorities
                );
            }
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<GrantedAuthority> authorities = user.getRoles() == null ? List.of() : user.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(AppConstants.ROLE_PREFIX + r.name()))
                .collect(Collectors.toList());

        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                null,
                currentTenant != null ? currentTenant : AppConstants.MASTER_TENANT_ID,
                AppConstants.USER_TYPE_TENANT,
                authorities
        );
    }
}