package com.oc.api.exception.types;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class TokenExpiredException extends BaseException {
    public TokenExpiredException(String message) {
        super(HttpStatus.UNAUTHORIZED.value(), "TOKEN_EXPIRED", message);
    }
}