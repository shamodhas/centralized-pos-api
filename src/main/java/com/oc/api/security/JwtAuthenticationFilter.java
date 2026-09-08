package com.oc.api.security;

import com.oc.api.constant.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws IOException, ServletException {

        try {
            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith(AppConstants.TOKEN_PREFIX)) {
                final String jwt = authHeader.substring(7);

                if (jwtService.isTokenValidWithoutUser(jwt)) {
                    final String email = jwtService.extractUsername(jwt);

                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        final String userType = jwtService.extractUserType(jwt);
                        final String tokenTenantId = jwtService.extractTenantId(jwt);

                        List<Map<String, String>> rawRoles = jwtService.extractRoles(jwt);
                        List<SimpleGrantedAuthority> authorities = rawRoles == null ? List.of() :
                                rawRoles.stream()
                                        .map(r -> new SimpleGrantedAuthority(r.get("authority")))
                                        .collect(Collectors.toList());

                        UserPrincipal principal = UserPrincipal.builder()
                                .username(email)
                                .tenantId(tokenTenantId)
                                .userType(userType)
                                .authorities(authorities)
                                .build();

                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}