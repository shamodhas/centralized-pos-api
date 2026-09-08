package com.oc.api.exception.types;

import lombok.Getter;
import lombok.Setter;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 6/19/2025 5:16 AM
 * Project: role-based-security-microservice
 * --------------------------------------------
 **/

@Getter
@Setter
public class InvalidInputException extends BaseException {
    public InvalidInputException(String message) {
        super(400, "INVALID_INPUT", message);
    }
}