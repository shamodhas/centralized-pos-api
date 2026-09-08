package com.oc.api.exception.types;

import lombok.Getter;
import lombok.Setter;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 6/19/2025 3:30 AM
 * Project: role-based-security-microservice
 * --------------------------------------------
 **/

@Getter
@Setter
public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String message) {
        super(404, "USER_NOT_FOUND", message);
    }
}