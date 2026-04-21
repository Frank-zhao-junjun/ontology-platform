package com.ontology.platform.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA配置
 * JPA Configuration
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.ontology.platform.infrastructure.repository"
)
@EntityScan(
        basePackages = "com.ontology.platform.infrastructure.persistence.entity"
)
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
    // JPA配置通过application.yml完成
}
