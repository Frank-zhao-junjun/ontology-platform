package com.ontology.platform.infrastructure.repository.upload;

import com.ontology.platform.common.enums.upload.ErrorHandling;
import com.ontology.platform.common.enums.upload.ImportStatus;
import com.ontology.platform.common.enums.upload.MergeStrategy;
import com.ontology.platform.domain.entity.upload.ImportTask;
import com.ontology.platform.domain.repository.upload.ImportTaskRepository;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.ImportTaskConverter;
import com.ontology.platform.infrastructure.persistence.ImportTaskPO;
import com.ontology.platform.infrastructure.persistence.ImportTaskPOMapper;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * ImportTaskRepositoryImpl 的 Testcontainers 集成测试（IT 后缀以示与单元测试区分）。
 * <p>
 * 与 mock 单元测试的关系：
 * <ul>
 *   <li>单元测试：{@link ImportTaskRepositoryImplTest} 覆盖契约/边界（不依赖 DB）。</li>
 *   <li>本 IT：在真实 PG 容器中跑完整 CRUD/Mapper 链路，验证 SQL、列映射、约束。</li>
 * </ul>
 * <p>
 * 优雅降级：本机/CI 节点无 Docker 时，{@code @Testcontainers(disabledWithoutDocker = true)}
 * 会自动跳过整个测试类，并打印明确原因，不会污染构建结果。
 */
@DisplayName("ImportTaskRepository 集成测试（Testcontainers PG）")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class ImportTaskRepositoryIT {

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
                    ImportTaskPOMapper.class,
                    ImportTaskConverter.class,
                    ImportTaskRepositoryImpl.class
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
    private ImportTaskRepository repository;

    @Autowired
    private ImportTaskPOMapper importTaskPOMapper;

    @BeforeEach
    void setUp() {
        importTaskPOMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ImportTaskPO>()
                        .ne("1", "1"));
    }

    @AfterEach
    void tearDown() {
        importTaskPOMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ImportTaskPO>()
                        .ne("1", "1"));
    }

    // ============================================================
    // 用例 1：save + findById 双向
    // ============================================================
    @Test
    @DisplayName("save + findById：插入后能按主键读回 Entity，字段一致")
    void save_andFindById_roundTrip() {
        ImportTask input = ImportTask.create(
                "upload-1", "ont-1", "Concept",
                MergeStrategy.INSERT, ErrorHandling.SKIP,
                "user-1", "tenant-1");

        ImportTask saved = repository.save(input);

        assertThat(saved.getId()).isEqualTo(input.getId());
        assertThat(saved.getUploadId()).isEqualTo("upload-1");
        assertThat(saved.getStatus()).isEqualTo(ImportStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<ImportTask> loaded = repository.findById(saved.getId());
        assertThat(loaded).isPresent();
        ImportTask reloaded = loaded.get();
        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getUploadId()).isEqualTo("upload-1");
        assertThat(reloaded.getOntologyId()).isEqualTo("ont-1");
        assertThat(reloaded.getStatus()).isEqualTo(ImportStatus.PENDING);
    }

    // ============================================================
    // 用例 2：save 保留字段值
    // ============================================================
    @Test
    @DisplayName("save：保留自定义 status 等字段")
    void save_preservesFields() {
        ImportTask task = ImportTask.create(
                "upload-2", "ont-2", "Concept",
                MergeStrategy.UPSERT, ErrorHandling.SKIP,
                "user-2", "tenant-2");
        task.setStatus(ImportStatus.PARSING);

        ImportTask saved = repository.save(task);

        assertThat(saved.getStatus()).isEqualTo(ImportStatus.PARSING);
        assertThat(saved.getMergeStrategy()).isEqualTo(MergeStrategy.UPSERT);
    }

    // ============================================================
    // 用例 3：findById 未命中
    // ============================================================
    @Test
    @DisplayName("findById：不存在的 ID 返回空 Optional")
    void findById_miss() {
        Optional<ImportTask> result = repository.findById("non-existent-id");
        assertThat(result).isEmpty();
    }

    // ============================================================
    // 用例 4：update 存在记录
    // ============================================================
    @Test
    @DisplayName("update：存在时更新成功")
    void update_existing() {
        ImportTask task = ImportTask.create(
                "upload-3", "ont-3", "Concept",
                MergeStrategy.INSERT, ErrorHandling.SKIP,
                "user-3", "tenant-3");
        repository.save(task);

        task.setStatus(ImportStatus.IMPORTING);
        task.updateProgress(50, 30, 20);

        ImportTask updated = repository.update(task);

        assertThat(updated.getStatus()).isEqualTo(ImportStatus.IMPORTING);
        assertThat(updated.getProcessedRows()).isEqualTo(50);
        assertThat(updated.getSuccessRows()).isEqualTo(30);
        assertThat(updated.getFailedRows()).isEqualTo(20);
    }

    // ============================================================
    // 用例 5：update 不存在抛异常
    // ============================================================
    @Test
    @DisplayName("update：不存在时抛 IllegalStateException")
    void update_notFound() {
        ImportTask task = ImportTask.create(
                "upload-ghost", "ont-ghost", "Concept",
                MergeStrategy.INSERT, ErrorHandling.SKIP,
                "user-x", "tenant-x");

        assertThatThrownBy(() -> repository.update(task))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ImportTask not found");
    }

    // ============================================================
    // 用例 6：连续 save 多条
    // ============================================================
    @Test
    @DisplayName("save：多条插入各自独立")
    void save_multiple() {
        ImportTask t1 = repository.save(ImportTask.create(
                "up-1", "ont-4", "Concept",
                MergeStrategy.INSERT, ErrorHandling.SKIP,
                "user-4", "tenant-4"));
        ImportTask t2 = repository.save(ImportTask.create(
                "up-2", "ont-4", "Event",
                MergeStrategy.UPSERT, ErrorHandling.STOP,
                "user-5", "tenant-4"));

        assertThat(t1.getId()).isNotEqualTo(t2.getId());
        assertThat(repository.findById(t1.getId())).isPresent();
        assertThat(repository.findById(t2.getId())).isPresent();
    }

    // ============================================================
    // 用例 7：标记完成
    // ============================================================
    @Test
    @DisplayName("save 后 markCompleted 再 update 验证 completion")
    void save_thenMarkCompleted() {
        ImportTask task = ImportTask.create(
                "upload-5", "ont-5", "Concept",
                MergeStrategy.INSERT, ErrorHandling.SKIP,
                "user-6", "tenant-6");
        repository.save(task);

        task.markCompleted();

        ImportTask updated = repository.update(task);

        assertThat(updated.getStatus()).isEqualTo(ImportStatus.COMPLETED);
        assertThat(updated.getCompletedAt()).isNotNull();
    }
}
