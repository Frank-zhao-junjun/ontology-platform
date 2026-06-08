package com.ontology.platform.common.constant;

/**
 * API常量定义
 */
public final class ApiConstants {

    private ApiConstants() {
        // 私有构造函数，防止实例化
    }

    // ==================== API路径 ====================
    public static final String API_V1 = "/v1";
    public static final String ONTOLOGIES = "/ontologies";
    public static final String OBJECT_TYPES = "/object-types";
    public static final String OBJECTS = "/objects";
    public static final String PROPERTIES = "/properties";
    public static final String RELATIONS = "/relations";
    public static final String ACTIONS = "/actions";
    public static final String QUERY = "/query";
    public static final String VALIDATE = "/validate";

    // ==================== 分页参数 ====================
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_CURSOR = "cursor";
    public static final String PARAM_SORT = "sort";
    public static final String PARAM_ORDER = "order";

    // ==================== 默认值 ====================
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // ==================== 请求头 ====================
    public static final String HEADER_API_KEY = "X-API-Key";
    public static final String HEADER_REQUEST_ID = "X-Request-ID";
    public static final String HEADER_TIMESTAMP = "X-Timestamp";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";

    // ==================== 认证 ====================
    public static final String AUTH_SCHEME = "Bearer";
    public static final String API_KEY_PREFIX = "sk_";

    // ==================== 日期时间格式 ====================
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    // ==================== 内容类型 ====================
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    // ==================== 图遍历常量 ====================
    public static final int DEFAULT_GRAPH_MAX_DEPTH = 5;
    public static final int DEFAULT_GRAPH_MAX_NODES = 1000;
    public static final int DEFAULT_GRAPH_TIMEOUT_MS = 5000;

    // ==================== 缓存常量 ====================
    public static final String CACHE_ONTOLOGY = "ontology";
    public static final String CACHE_OBJECT_TYPE = "object_type";
    public static final String CACHE_PROPERTY = "property";
    public static final String CACHE_RELATION = "relation";
}
