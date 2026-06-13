package com.ontology.platform.infrastructure.testsupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Testcontainers 单例 PostgreSQL 容器（携带 Apache AGE 扩展）。
 * <p>
 * 设计要点：
 * <ul>
 *   <li>JVM 内单例：static final 容器，@Container 语义避免重复启动。</li>
 *   <li>环境无 Docker 时：本类不会抛错；{@link #isAvailable()} 返回 false，
 *       集成测试可基于该信号 {@code Assumptions.assumeTrue(...)} 优雅跳过。</li>
 *   <li>启动成功后自动执行 {@code schema-testcontainers.sql} 建表，避免依赖 Flyway。</li>
 *   <li>提供 {@link #registerProperties(DynamicPropertyRegistry)} 给 IT 绑定 Spring 属性。</li>
 * </ul>
 * <p>
 * 用法（在 IT 类上）：
 * <pre>{@code
 * @SpringBootTest(classes = RelationRepositoryIT.MinimalConfig.class,
 *                 properties = "spring.config.name=application-testcontainers")
 * class RelationRepositoryIT {
 *     static {
 *         PostgresTestContainer.startIfNeeded();
 *     }
 *
 *     @BeforeAll
 *     static void guard() {
 *         Assumptions.assumeTrue(PostgresTestContainer.isAvailable(),
 *                 "Docker 不可用，跳过 Testcontainers 集成测试");
 *     }
 *
 *     @DynamicPropertySource
 *     static void wire(DynamicPropertyRegistry r) {
 *         PostgresTestContainer.registerProperties(r);
 *     }
 * }
 * }</pre>
 */
public final class PostgresTestContainer {

    private static final Logger log = LoggerFactory.getLogger(PostgresTestContainer.class);

    /** Apache AGE 官方镜像。版本与 PG 15 对齐，扩展预装。 */
    private static final DockerImageName AGE_IMAGE = DockerImageName
            .parse("apache/age")
            .asCompatibleSubstituteFor("postgres")
            .withTag("PG15_latest");

    /** 单例容器：static 字段仅在首次访问时触发启动逻辑。 */
    @SuppressWarnings("resource") // 由 JVM 生命周期统一管理
    private static final PostgreSQLContainer<?> CONTAINER = new PostgreSQLContainer<>(AGE_IMAGE)
            .withDatabaseName("ontology_it")
            .withUsername("ontology_it")
            .withPassword("ontology_it")
            .withReuse(true)
            .withLabel("ontology-platform", "testcontainers")
            .withStartupTimeoutSeconds(180);

    /** 是否已经成功启动。 */
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private PostgresTestContainer() {
        // 工具类，禁止实例化
    }

    /**
     * 主动触发容器启动。失败时不会抛错，仅记录日志并保持 {@link #isAvailable()} 返回 false。
     * 集成测试可在 static 初始化块中调用。
     */
    public static synchronized void startIfNeeded() {
        if (STARTED.get()) {
            return;
        }
        try {
            if (!CONTAINER.isRunning()) {
                CONTAINER.start();
            }
            initSchema();
            STARTED.set(true);
            log.info("Testcontainers PostgreSQL(AGE) 已就绪：jdbcUrl={}, host={}, port={}",
                    CONTAINER.getJdbcUrl(), CONTAINER.getHost(), CONTAINER.getFirstMappedPort());
        } catch (Throwable t) {
            // Docker 不可用、镜像拉取失败等都在此兜底
            log.warn("Testcontainers PostgreSQL 启动失败，本机/该 CI 节点将跳过 IT：{}",
                    t.getMessage());
        }
    }

    /**
     * 容器是否成功启动。{@code false} 时集成测试应跳过。
     */
    public static boolean isAvailable() {
        return STARTED.get() && CONTAINER.isRunning();
    }

    /**
     * 暴露 JDBC URL。仅在 {@link #isAvailable()} 为 true 时可调用。
     */
    public static String getJdbcUrl() {
        ensureStarted();
        return CONTAINER.getJdbcUrl();
    }

    /**
     * 暴露用户名。
     */
    public static String getUsername() {
        ensureStarted();
        return CONTAINER.getUsername();
    }

    /**
     * 暴露密码。
     */
    public static String getPassword() {
        ensureStarted();
        return CONTAINER.getPassword();
    }

    /**
     * 把容器连接信息绑定到 Spring 动态属性（被 @DynamicPropertySource 调用）。
     */
    public static void registerProperties(DynamicPropertyRegistry registry) {
        if (!isAvailable()) {
            throw new IllegalStateException(
                    "PostgresTestContainer 不可用，无法注册 Spring 属性（Docker 缺失？）");
        }
        registry.add("spring.datasource.url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", CONTAINER::getUsername);
        registry.add("spring.datasource.password", CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    /**
     * 容器启动后立即执行最小化 schema，建出 relation_definition 等表。
     * <p>
     * 不复用 {@code schema.sql}，原因：H2 PostgreSQL 模式下的 schema.sql 字段类型
     * （TEXT/TIMESTAMP WITH TIME ZONE）已足够；这里我们用更精简的建表，避免依赖 AGE 扩展
     * 启动顺序带来的兼容问题。
     * </p>
     */
    private static void initSchema() {
        String ddl = """
                CREATE TABLE IF NOT EXISTS ontology (
                    id VARCHAR(36) PRIMARY KEY,
                    tenant_id VARCHAR(36) NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001',
                    name VARCHAR(100) NOT NULL,
                    display_name VARCHAR(200) NOT NULL,
                    description TEXT,
                    version VARCHAR(20) NOT NULL DEFAULT '0.1.0',
                    status VARCHAR(20) NOT NULL DEFAULT 'draft',
                    published_at TIMESTAMP WITH TIME ZONE,
                    object_type_count INT DEFAULT 0,
                    action_type_count INT DEFAULT 0,
                    created_by VARCHAR(36),
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                );

                CREATE TABLE IF NOT EXISTS object_type (
                    id VARCHAR(36) PRIMARY KEY,
                    ontology_id VARCHAR(36) NOT NULL,
                    name VARCHAR(100) NOT NULL,
                    display_name VARCHAR(200) NOT NULL,
                    description TEXT,
                    primary_key VARCHAR(100) NOT NULL,
                    parent_id VARCHAR(36),
                    instance_count INT DEFAULT 0,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
                );

                CREATE TABLE IF NOT EXISTS relation_definition (
                    id VARCHAR(36) PRIMARY KEY,
                    ontology_id VARCHAR(36) NOT NULL,
                    source_type_id VARCHAR(36) NOT NULL,
                    target_type_id VARCHAR(36) NOT NULL,
                    name VARCHAR(100) NOT NULL,
                    display_name VARCHAR(200) NOT NULL,
                    description TEXT,
                    cardinality VARCHAR(10) NOT NULL DEFAULT '1:N',
                    reverse_name VARCHAR(100),
                    reverse_display_name VARCHAR(200),
                    extended_data TEXT,
                    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT uk_relation_ontology_name UNIQUE (ontology_id, name)
                );

                CREATE INDEX IF NOT EXISTS idx_relation_ontology ON relation_definition(ontology_id);
                """;
        try (Connection conn = DriverManager.getConnection(
                CONTAINER.getJdbcUrl(), CONTAINER.getUsername(), CONTAINER.getPassword());
             Statement stmt = conn.createStatement()) {
            stmt.execute(ddl);
            log.info("IT 最小 schema 已初始化（ontology/object_type/relation_definition）");
        } catch (SQLException e) {
            throw new IllegalStateException("初始化 IT schema 失败", e);
        }
    }

    private static void ensureStarted() {
        if (!isAvailable()) {
            throw new IllegalStateException(
                    "PostgresTestContainer 尚未启动或启动失败（Docker 不可用？）");
        }
    }
}
