package com.oc.api.exception.types;

import lombok.Getter;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 6/19/2025 4:16 AM
 * Project: role-based-security-microservice
 * --------------------------------------------
 **/

@Getter
public abstract class BaseException extends RuntimeException {

    private final int status;
    private final String errorCode;
    private final String message;
    private final Object details;

    public BaseException(int status, String errorCode, String message) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.details = null;
    }

    public BaseException(int status, String errorCode, String message, Object details) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
    }
}