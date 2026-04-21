package com.ontology.platform.common.enums;

/**
 * 错误码枚举
 * 遵循API规范中的错误码定义
 */
public enum ErrorCode {

    // 系统错误 (1000-1999)
    SUCCESS(0, "success"),
    INTERNAL_ERROR(1001, "Internal server error"),
    SERVICE_UNAVAILABLE(1002, "Service unavailable"),
    DATABASE_ERROR(1003, "Database error"),

    // 认证授权错误 (2000-2999)
    UNAUTHORIZED(2001, "Unauthorized"),
    INVALID_API_KEY(2002, "Invalid API key"),
    TOKEN_EXPIRED(2003, "Token expired"),
    INVALID_TOKEN(2004, "Invalid token"),

    // 参数校验错误 (3000-3999)
    VALIDATION_ERROR(3001, "Validation error"),
    MISSING_PARAMETER(3002, "Missing required parameter"),
    INVALID_PARAMETER(3003, "Invalid parameter format"),

    // 业务逻辑错误 (4000-4999)
    ONTOLOGY_NOT_FOUND(4001, "Ontology not found"),
    OBJECT_TYPE_NOT_FOUND(4002, "Object type not found"),
    OBJECT_NOT_FOUND(4003, "Object instance not found"),
    ACTION_NOT_FOUND(4004, "Action not found"),
    RELATION_NOT_FOUND(4005, "Relation not found"),
    VALIDATION_FAILED(4006, "Validation failed"),
    RULE_VIOLATED(4007, "Rule violated"),

    // 权限错误 (5000-5999)
    PERMISSION_DENIED(5001, "Permission denied"),
    READ_ONLY_RESOURCE(5002, "Resource is read-only"),

    // 资源不存在 (6000-6999)
    RESOURCE_NOT_FOUND(6001, "Resource not found"),

    // 资源冲突 (7000-7999)
    RESOURCE_CONFLICT(7001, "Resource conflict"),
    DUPLICATE_NAME(7002, "Duplicate name"),
    DUPLICATE_ENTRY(7003, "Duplicate entry"),

    // 外部服务错误 (8000-8999)
    DATA_SOURCE_ERROR(8001, "Data source connection error"),
    SYNC_FAILED(8002, "Synchronization failed"),

    // 图遍历错误 (9000-9999)
    GRAPH_TRAVERSAL_ERROR(9001, "Graph traversal error"),
    INVALID_TRAVERSAL_REQUEST(9002, "Invalid traversal request"),
    INVALID_FILTER(9003, "Invalid filter parameter"),
    INVALID_RELATION_TYPE(9004, "Invalid relation type"),
    INVALID_OBJECT_TYPE(9005, "Invalid object type"),
    TRAVERSAL_DEPTH_EXCEEDED(9006, "Traversal depth exceeds maximum limit"),
    TRAVERSAL_LIMIT_EXCEEDED(9007, "Traversal result limit exceeded"),
    // 文件上传错误 (9100-9199)
    FILE_TOO_LARGE(9101, "File size exceeds limit"),
    UNSUPPORTED_FILE_TYPE(9102, "Unsupported file type"),
    UPLOAD_EXPIRED(9103, "Upload task expired"),
    CHUNK_OUT_OF_RANGE(9104, "Chunk number out of range"),
    CHUNK_MD5_MISMATCH(9105, "Chunk MD5 mismatch"),
    UPLOAD_NOT_FOUND(9106, "Upload task not found"),
    CHUNK_ALREADY_EXISTS(9107, "Chunk already uploaded"),
    FILE_VERIFICATION_FAILED(9108, "File verification failed"),

    // 导入导出错误 (9200-9299)
    IMPORT_FILE_PARSING_ERROR(9201, "Import file parsing error"),
    IMPORT_COLUMN_MAPPING_ERROR(9202, "Column mapping error"),
    IMPORT_VALIDATION_ERROR(9203, "Import data validation error"),
    IMPORT_OBJECT_TYPE_NOT_FOUND(9204, "Object type not found for import"),
    EXPORT_GENERATION_ERROR(9205, "Export file generation error"),
    IMPORT_TEMPLATE_NOT_FOUND(9206, "Import template not found");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取枚举
     */
    public static ErrorCode fromCode(int code) {
        for (ErrorCode errorCode : values()) {
            if (errorCode.code == code) {
                return errorCode;
            }
        }
        return INTERNAL_ERROR;
    }
}
