package com.oc.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private Long id;
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private String email;
    private String tenantId;
    private String userType;
    private Set<String> roles;
}