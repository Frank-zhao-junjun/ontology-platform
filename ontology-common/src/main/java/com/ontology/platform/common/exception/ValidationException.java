package com.ontology.platform.common.exception;

import com.ontology.platform.common.enums.ErrorCode;

/**
 * 参数校验异常
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }

    public ValidationException(String message, String details) {
        super(ErrorCode.VALIDATION_ERROR, message, details);
    }
}
