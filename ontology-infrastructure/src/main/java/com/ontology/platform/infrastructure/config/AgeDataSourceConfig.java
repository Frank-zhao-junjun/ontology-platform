package com.ontology.platform.infrastructure.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Apache AGE 数据源配置
 * Apache AGE Datasource Configuration
 * 
 * 配置专用于图查询的连接池
 */
@Configuration
public class AgeDataSourceConfig {
    
    @Value("${spring.datasource.url:jdbc:postgresql://localhost:5432/ontology}")
    private String jdbcUrl;
    
    @Value("${spring.datasource.username:postgres}")
    private String username;
    
    @Value("${spring.datasource.password:postgres}")
    private String password;

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;
    
    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;
    
    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;
    
    @Value("${ontology.graph.connection-timeout:30000}")
    private int connectionTimeout;
    
    @Value("${ontology.graph.idle-timeout:600000}")
    private int idleTimeout;
    
    @Value("${ontology.graph.max-lifetime:1800000}")
    private int maxLifetime;
    
    /**
     * 创建AGE图查询专用数据源
     */
    @Bean(name = "ageDataSource")
    @Primary
    public DataSource ageDataSource() {
        HikariConfig config = new HikariConfig();
        
        // 基本配置
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // 连接池配置
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setConnectionTimeout(connectionTimeout);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        
        // 性能优化
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        
        // 连接测试
        config.setConnectionTestQuery("SELECT 1");
        
        // 池名称
        config.setPoolName("AGE-HikariPool");
        
        return new HikariDataSource(config);
    }
}
