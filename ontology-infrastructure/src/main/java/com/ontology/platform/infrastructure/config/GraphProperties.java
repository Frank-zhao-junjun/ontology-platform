package com.ontology.platform.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 图遍历配置属性
 * Graph Traversal Configuration Properties
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ontology.graph")
public class GraphProperties {
    
    /**
     * 查询超时时间（秒）
     */
    private int queryTimeout = 30;
    
    /**
     * 最大结果集大小
     */
    private int maxResultSize = 10000;
    
    /**
     * 连接超时时间（毫秒）
     */
    private int connectionTimeout = 30000;
    
    /**
     * 空闲超时时间（毫秒）
     */
    private int idleTimeout = 600000;
    
    /**
     * 最大生命周期（毫秒）
     */
    private int maxLifetime = 1800000;
    
    /**
     * 最大遍历深度
     */
    private int maxTraversalDepth = 5;
    
    /**
     * 最大结果限制
     */
    private int maxResultLimit = 1000;
    
    /**
     * 默认遍历深度
     */
    private int defaultTraversalDepth = 3;
    
    /**
     * 默认结果限制
     */
    private int defaultResultLimit = 100;
    
    /**
     * 是否启用查询缓存
     */
    private boolean queryCacheEnabled = true;
    
    /**
     * 查询缓存TTL（分钟）
     */
    private int queryCacheTtlMinutes = 30;

    /**
     * 是否启用降级模式
     * <p>开启后图相关调用将记录警告但不会抛错，便于在无 Apache AGE 环境下继续运行业务功能。</p>
     */
    private boolean degraded = false;
}
