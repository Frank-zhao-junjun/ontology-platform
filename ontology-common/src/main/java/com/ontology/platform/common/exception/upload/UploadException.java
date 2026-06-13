package com.ontology.platform.common.exception.upload;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;

public class UploadException extends BusinessException {
    public UploadException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static UploadException fileTooLarge(long maxFileSize) {
        return new UploadException(ErrorCode.VALIDATION_ERROR, "File exceeds max size: " + maxFileSize);
    }

    public static UploadException unsupportedFileType(String fileType) {
        return new UploadException(ErrorCode.VALIDATION_ERROR, "Unsupported file type: " + fileType);
    }

    public static UploadException uploadNotFound(String uploadId) {
        return new UploadException(ErrorCode.RESOURCE_NOT_FOUND, "Upload not found: " + uploadId);
    }

    public static UploadException uploadExpired(String uploadId) {
        return new UploadException(ErrorCode.VALIDATION_ERROR, "Upload expired: " + uploadId);
    }

    public static UploadException chunkOutOfRange(int chunkNumber, int totalChunks) {
        return new UploadException(ErrorCode.VALIDATION_ERROR,
                "Chunk " + chunkNumber + " out of range 1.." + totalChunks);
    }

    public static UploadException fileVerificationFailed() {
        return new UploadException(ErrorCode.VALIDATION_ERROR, "File verification failed");
    }
}
