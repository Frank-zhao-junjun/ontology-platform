package com.ontology.platform.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 安全配置
 * - 基于 Token 的认证（X-API-Key header）
 * - CORS 白名单
 * - 写操作需要认证，读操作宽松
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 安全过滤链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // Actuator & Swagger — 允许匿名访问
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 读操作 (GET) — 允许匿名访问
                .requestMatchers(HttpMethod.GET, "/api/**", "/v1/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 写操作 (POST/PUT/PATCH/DELETE) — 需要认证
                .requestMatchers(HttpMethod.POST, "/api/**", "/v1/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/**", "/v1/**").authenticated()
                .requestMatchers(HttpMethod.PATCH, "/api/**", "/v1/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/**", "/v1/**").authenticated()
                .anyRequest().permitAll()
            )
            .httpBasic(hb -> hb.disable())
            .formLogin(fl -> fl.disable());

        return http.build();
    }

    /**
     * CORS 配置 — 仅允许白名单来源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",   // Vite dev server
            "http://localhost:3000",   // MCP server
            "http://localhost:8080"    // self
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
