package com.ontology.platform.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 本体建模平台应用入口
 * Ontology Platform Application Entry Point
 */
@SpringBootApplication(
        scanBasePackages = "com.ontology.platform"
)
@MapperScan("com.ontology.platform.infrastructure.repository.mapper")
@EnableConfigurationProperties
@EnableCaching
@EnableAsync
@EnableScheduling
public class OntologyPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(OntologyPlatformApplication.class, args);
    }
}
