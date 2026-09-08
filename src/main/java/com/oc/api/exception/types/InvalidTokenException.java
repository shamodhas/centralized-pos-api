package com.oc.api.exception.types;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class InvalidTokenException extends BaseException {
    public InvalidTokenException(String message) {
        super(HttpStatus.BAD_REQUEST.value(), "INVALID_TOKEN", message, null);
    }
}