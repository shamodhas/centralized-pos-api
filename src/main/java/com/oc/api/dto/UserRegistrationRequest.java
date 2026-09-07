package com.oc.api.dto;

import com.oc.api.model.tenant.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserRegistrationRequest {
    private String email;
    private String password;
    private Set<Role> roles;
}