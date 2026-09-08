package com.oc.api.exception.types;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

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
public class DuplicationException extends BaseException {
    public DuplicationException(String message) {
        super(HttpStatus.CONFLICT.value(), "DUPLICATE_RESOURCE", message);
    }
}