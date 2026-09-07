package com.oc.api.security;

import com.oc.api.constant.AppConstants;
import com.oc.api.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(AppConstants.HEADER_AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith(AppConstants.TOKEN_PREFIX)) {
            final String jwt = authHeader.substring(7);
            final String email = jwtService.extractUsername(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                final String userType = jwtService.extractClaim(jwt, claims -> claims.get(AppConstants.CLAIM_USER_TYPE, String.class), false);
                final String tokenTenantId = jwtService.extractClaim(jwt, claims -> claims.get(AppConstants.CLAIM_TENANT_ID, String.class), false);

                if (AppConstants.USER_TYPE_GLOBAL.equals(userType)) {
                    String overrideTenant = request.getHeader(AppConstants.HEADER_TENANT_ID);
                    TenantContext.setCurrentTenant(overrideTenant != null ? overrideTenant : AppConstants.MASTER_TENANT_ID);
                } else if (tokenTenantId != null) {
                    TenantContext.setCurrentTenant(tokenTenantId);
                }

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}