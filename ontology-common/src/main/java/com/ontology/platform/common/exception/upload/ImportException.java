package com.ontology.platform.common.exception.upload;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;

public class ImportException extends BusinessException {
    public ImportException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static ImportException parsingError(String target, String message) {
        return new ImportException(ErrorCode.VALIDATION_ERROR, "Parsing failed for " + target + ": " + message);
    }

    public static ImportException objectTypeNotFound(String objectType) {
        return new ImportException(ErrorCode.OBJECT_TYPE_NOT_FOUND, "Object type not found: " + objectType);
    }
}
