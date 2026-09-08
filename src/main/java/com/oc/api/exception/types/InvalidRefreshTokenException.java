package com.oc.api.exception.types;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 6/25/2025 12:29 PM
 * Project: erp-backend
 * --------------------------------------------
 **/

public class InvalidRefreshTokenException extends BaseException {
    public InvalidRefreshTokenException(String message) {
        super(401, "INVALID_REFRESH_TOKEN", message);
    }

    public InvalidRefreshTokenException(String message, Object details) {
        super(401, "INVALID_REFRESH_TOKEN", message, details);
    }
}
