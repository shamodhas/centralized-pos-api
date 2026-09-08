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
public class InternalServerErrorException extends BaseException {
    public InternalServerErrorException(String message) {
        super(500, "INTERNAL_SERVER_ERROR", message);
    }
}
