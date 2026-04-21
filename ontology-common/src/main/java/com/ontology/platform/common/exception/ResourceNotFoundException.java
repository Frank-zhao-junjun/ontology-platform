package com.ontology.platform.common.exception;

import com.ontology.platform.common.enums.ErrorCode;

/**
 * 资源不存在异常
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(ErrorCode.RESOURCE_NOT_FOUND,
              String.format("%s not found with id: %s", resourceType, resourceId));
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }
}
