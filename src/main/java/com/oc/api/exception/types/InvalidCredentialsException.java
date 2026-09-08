package com.oc.api.exception.types;

import lombok.Getter;
import lombok.Setter;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 6/19/2025 4:07 AM
 * Project: role-based-security-microservice
 * --------------------------------------------
 **/

@Getter
@Setter
public class InvalidCredentialsException extends BaseException {
    public InvalidCredentialsException(String message) {
        super(401, "INVALID_CREDENTIALS", message);
    }
}
