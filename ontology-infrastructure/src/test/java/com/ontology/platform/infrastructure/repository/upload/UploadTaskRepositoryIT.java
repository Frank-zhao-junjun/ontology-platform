package com.ontology.platform.infrastructure.repository.upload;

import com.ontology.platform.common.enums.upload.FileType;
import com.ontology.platform.common.enums.upload.UploadStatus;
import com.ontology.platform.domain.entity.upload.UploadTask;
import com.ontology.platform.domain.repository.upload.UploadTaskRepository;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.UploadTaskConverter;
import com.ontology.platform.infrastructure.persistence.UploadTaskPO;
import com.ontology.platform.infrastructure.persistence.UploadTaskPOMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UploadTaskRepositoryImpl 的 Testcontainers 集成测试（IT 后缀以示与单元测试区分）。
 * <p>
 * 与 mock 单元测试的关系：
 * <ul>
 *   <li>单元测试：{@link UploadTaskRepositoryImplTest} 覆盖契约/边界（不依赖 DB）。</li>
 *   <li>本 IT：在真实 PG 容器中跑完整 CRUD/Mapper 链路，验证 SQL、列映射、约束。</li>
 * </ul>
 * <p>
 * 优雅降级：本机/CI 节点无 Docker 时，{@code @Testcontainers(disabledWithoutDocker = true)}
 * 会自动跳过整个测试类，并打印明确原因，不会污染构建结果。
 */
@DisplayName("UploadTaskRepository 集成测试（Testcontainers PG）")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class UploadTaskRepositoryIT {

    /**
     * 单例 PG 容器：{@code @Container} 配合 {@code @Testcontainers} 扩展。
     */
    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            DockerImageName.parse("apache/age")
                    .asCompatibleSubstituteFor("postgres")
                    .withTag("PG15_latest"))
            .withDatabaseName("ontology_it")
            .withUsername("ontology_it")
            .withPassword("ontology_it")
            .withReuse(true)
            .withLabel("ontology-platform", "testcontainers")
            .withStartupTimeoutSeconds(180);

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @SpringBootConfiguration
    @ImportAutoConfiguration({
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class
    })
    @ComponentScan(
            basePackageClasses = {
                    UploadTaskPOMapper.class,
                    UploadTaskConverter.class,
                    UploadTaskRepositoryImpl.class
            },
            excludeFilters = {
                    @ComponentScan.Filter(
                            type = FilterType.REGEX,
                            pattern = {
                                    "com\\.ontology\\.platform\\.infrastructure\\.config\\.RedisConfig",
                                    "com\\.ontology\\.platform\\.infrastructure\\.config\\.AgeDataSourceConfig",
                                    "com\\.ontology\\.platform\\.infrastructure\\.config\\.JpaConfig"
                            }
                    )
            }
    )
    @EnableTransactionManagement
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        public MyBatisPlusConfig myBatisPlusConfig() {
            return new MyBatisPlusConfig();
        }
    }

    @Autowired
    private UploadTaskRepository repository;

    @Autowired
    private UploadTaskPOMapper uploadTaskPOMapper;

    @BeforeEach
    void setUp() {
        uploadTaskPOMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UploadTaskPO>()
                        .ne("1", "1"));
    }

    @AfterEach
    void tearDown() {
        uploadTaskPOMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UploadTaskPO>()
                        .ne("1", "1"));
    }

    // ============================================================
    // 用例 1：save + findById 双向
    // ============================================================
    @Test
    @DisplayName("save + findById：插入后能按主键读回 Entity，字段一致")
    void save_andFindById_roundTrip() {
        UploadTask input = UploadTask.create(
                "test.csv", 1024, FileType.CSV, 256,
                "import", "ont-1", "Concept", "user-1", "tenant-1");
        input.setCreatedAt(null); // 让 save 填充

        UploadTask saved = repository.save(input);

        assertThat(saved.getId()).isEqualTo(input.getId());
        assertThat(saved.getOriginalFileName()).isEqualTo("test.csv");
        assertThat(saved.getFileSize()).isEqualTo(1024);
        assertThat(saved.getFileType()).isEqualTo(FileType.CSV);
        assertThat(saved.getStatus()).isEqualTo(UploadStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Optional<UploadTask> loaded = repository.findById(saved.getId());
        assertThat(loaded).isPresent();
        UploadTask reloaded = loaded.get();
        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getOriginalFileName()).isEqualTo("test.csv");
        assertThat(reloaded.getFileSize()).isEqualTo(1024);
        assertThat(reloaded.getFileType()).isEqualTo(FileType.CSV);
        assertThat(reloaded.getStatus()).isEqualTo(UploadStatus.PENDING);
    }

    // ============================================================
    // 用例 2：save 保留已有 createdAt
    // ============================================================
    @Test
    @DisplayName("save：已有 createdAt 时不覆盖")
    void save_preservesExistingCreatedAt() {
        Instant fixed = Instant.now().minusSeconds(3600);
        UploadTask task = UploadTask.create(
                "preserve.csv", 2048, FileType.CSV, 512,
                "import", "ont-2", "Concept", "user-2", "tenant-2");
        task.setCreatedAt(fixed);

        UploadTask saved = repository.save(task);

        assertThat(saved.getCreatedAt()).isEqualTo(fixed);
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    // ============================================================
    // 用例 3：findById 未命中
    // ============================================================
    @Test
    @DisplayName("findById：不存在的 ID 返回空 Optional")
    void findById_miss() {
        Optional<UploadTask> result = repository.findById("non-existent-id");
        assertThat(result).isEmpty();
    }

    // ============================================================
    // 用例 4：update 存在记录
    // ============================================================
    @Test
    @DisplayName("update：存在时更新并刷新 updatedAt")
    void update_existing() {
        UploadTask task = UploadTask.create(
                "update.csv", 512, FileType.CSV, 256,
                "import", "ont-3", "Concept", "user-3", "tenant-3");
        repository.save(task);

        // 模拟标记完成
        task.markCompleted("/tmp/stored.csv", "abc123");

        UploadTask updated = repository.update(task);

        assertThat(updated.getStoredFilePath()).isEqualTo("/tmp/stored.csv");
        assertThat(updated.getFileMd5()).isEqualTo("abc123");
        assertThat(updated.getStatus()).isEqualTo(UploadStatus.COMPLETED);
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isAfter(task.getCreatedAt());
    }

    // ============================================================
    // 用例 5：update 不存在抛异常
    // ============================================================
    @Test
    @DisplayName("update：不存在时抛 IllegalStateException")
    void update_notFound() {
        UploadTask task = UploadTask.create(
                "ghost.csv", 128, FileType.CSV, 128,
                "import", "ont-4", "Concept", "user-4", "tenant-4");

        assertThatThrownBy(() -> repository.update(task))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UploadTask not found");
    }

    // ============================================================
    // 用例 6：deleteById 删除
    // ============================================================
    @Test
    @DisplayName("deleteById：删除后 findById 返回空")
    void deleteById_removes() {
        UploadTask task = UploadTask.create(
                "delete.csv", 256, FileType.CSV, 256,
                "import", "ont-5", "Concept", "user-5", "tenant-5");
        repository.save(task);

        assertThat(repository.findById(task.getId())).isPresent();

        repository.deleteById(task.getId());

        assertThat(repository.findById(task.getId())).isEmpty();
    }

    // ============================================================
    // 用例 7：连续 save 多条，互不影响
    // ============================================================
    @Test
    @DisplayName("save：多条插入各自独立 ID")
    void save_multipleTasks() {
        UploadTask t1 = repository.save(UploadTask.create(
                "a.csv", 100, FileType.CSV, 256,
                "import", "ont-6", "Concept", "user-6", "tenant-6"));
        UploadTask t2 = repository.save(UploadTask.create(
                "b.csv", 200, FileType.CSV, 256,
                "import", "ont-6", "Concept", "user-7", "tenant-6"));

        assertThat(t1.getId()).isNotEqualTo(t2.getId());
        assertThat(repository.findById(t1.getId())).isPresent();
        assertThat(repository.findById(t2.getId())).isPresent();
    }
}
