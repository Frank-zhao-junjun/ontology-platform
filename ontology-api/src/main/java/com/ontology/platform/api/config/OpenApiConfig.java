package com.ontology.platform.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI配置
 * OpenAPI Configuration
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "apiKey";

        return new OpenAPI()
                .info(new Info()
                        .title("Ontology Platform API")
                        .version("1.0.0")
                        .description("本体模型服务平台REST API文档\n\n" +
                                "## 认证方式\n" +
                                "- API Key认证：在请求头中添加 `X-API-Key`\n\n" +
                                "## 通用响应格式\n" +
                                "所有API响应都遵循统一的格式：\n" +
                                "- `code`: 状态码，0表示成功\n" +
                                "- `message`: 状态消息\n" +
                                "- `data`: 响应数据\n" +
                                "- `meta`: 元信息（包含请求ID和时间戳）")
                        .contact(new Contact()
                                .name("Ontology Platform Team")
                                .email("support@ontology-platform.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + "/api")
                                .description("开发环境服务器"),
                        new Server()
                                .url("https://api.ontology-platform.com")
                                .description("生产环境服务器")))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .description("API Key认证")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}
