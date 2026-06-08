package com.ontology.platform.api.dto;

import java.util.UUID;

/**
 * 请求上下文
 * Request Context
 */
public class RequestContext {

    private static final ThreadLocal<String> REQUEST_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ID = new ThreadLocal<>();

    private RequestContext() {
        // 私有构造函数
    }

    public static String getRequestId() {
        String requestId = REQUEST_ID.get();
        if (requestId == null) {
            requestId = "req_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            REQUEST_ID.set(requestId);
        }
        return requestId;
    }

    public static void setRequestId(String requestId) {
        REQUEST_ID.set(requestId);
    }

    public static String getUserId() {
        return USER_ID.get();
    }

    public static void setUserId(String userId) {
        USER_ID.set(userId);
    }

    public static void clear() {
        REQUEST_ID.remove();
        USER_ID.remove();
    }
}
