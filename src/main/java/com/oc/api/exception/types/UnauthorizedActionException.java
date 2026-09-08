package com.oc.api.exception.types;

import lombok.Getter;
import lombok.Setter;

/**
 * --------------------------------------------
 * Author: Shamodha Sahan
 * GitHub: https://github.com/shamodhas
 * Website: https://shamodha.com
 * --------------------------------------------
 * Created: 7/4/2025 5:56 AM
 * Project: erp-backend
 * --------------------------------------------
 **/

@Getter
@Setter
public class UnauthorizedActionException extends BaseException {

    public UnauthorizedActionException(String message) {
        super(403, "UNAUTHORIZED_ACTION", message);
    }
}