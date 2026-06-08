package com.ontology.platform.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis-Plus配置
 * MyBatis-Plus Configuration
 */
@Configuration
@EnableTransactionManagement
@MapperScan(
        basePackages = "com.ontology.platform.infrastructure.persistence"
)
public class MyBatisPlusConfig {
    // MyBatis-Plus配置通过application.yml完成
}
