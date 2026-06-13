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
    QUERY_TIMEOUT(1004, "Query timeout"),
    GRAPH_TRAVERSAL_ERROR(1005, "Graph traversal error"),
    NOT_IMPLEMENTED(1006, "Not implemented"),

    // 认证授权错误 (2000-2999)
    UNAUTHORIZED(2001, "Unauthorized"),
    INVALID_API_KEY(2002, "Invalid API key"),
    TOKEN_EXPIRED(2003, "Token expired"),
    INVALID_TOKEN(2004, "Invalid token"),

    // 参数校验错误 (3000-3999)
    VALIDATION_ERROR(3001, "Validation error"),
    MISSING_PARAMETER(3002, "Missing required parameter"),
    INVALID_PARAMETER(3003, "Invalid parameter format"),
    INVALID_FILTER(3004, "Invalid filter"),
    INVALID_FIELD_NAME(3005, "Invalid field name"),
    INVALID_OPERATOR(3006, "Invalid operator"),
    INVALID_TRAVERSAL_REQUEST(3007, "Invalid traversal request"),
    INVALID_OBJECT_TYPE(3008, "Invalid object type"),
    INVALID_RELATION_TYPE(3009, "Invalid relation type"),

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

    // 外部服务错误 (8000-8999)
    DATA_SOURCE_ERROR(8001, "Data source connection error"),
    SYNC_FAILED(8002, "Synchronization failed"),
    QUERY_TIMEOUT(8003, "Query execution timeout"),
    GRAPH_TRAVERSAL_ERROR(8004, "Graph traversal error");

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
