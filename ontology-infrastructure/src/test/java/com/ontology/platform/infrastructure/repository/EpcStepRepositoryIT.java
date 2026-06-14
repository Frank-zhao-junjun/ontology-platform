package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.EpcStep;
import com.ontology.platform.domain.repository.EpcStepRepository;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.EpcStepConverter;
import com.ontology.platform.infrastructure.persistence.EpcStepPO;
import com.ontology.platform.infrastructure.persistence.EpcStepPOMapper;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EpcStepRepositoryImpl 的 Testcontainers 集成测试（IT 后缀以示与单元测试区分）。
 * <p>
 * 与 mock 单元测试的关系：
 * <ul>
 *   <li>单元测试：{@link EpcStepRepositoryImplTest} 覆盖契约/边界（不依赖 DB）。</li>
 *   <li>本 IT：在真实 PG（携带 AGE 扩展）容器中跑完整 CRUD/Mapper 链路，验证 SQL、列映射、约束。</li>
 * </ul>
 * <p>
 * 优雅降级：本机/CI 节点无 Docker 时，{@code @Testcontainers(disabledWithoutDocker = true)}
 * 会自动跳过整个测试类，并打印明确原因，不会污染构建结果。
 */
@DisplayName("EpcStepRepository 集成测试（Testcontainers PG+AGE）")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class EpcStepRepositoryIT {

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
                    EpcStepPOMapper.class,
                    EpcStepConverter.class,
                    EpcStepRepositoryImpl.class
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
    private EpcStepRepository repository;

    @Autowired
    private EpcStepPOMapper epcStepPOMapper;

    private String ontologyId;
    private String flowName;

    @BeforeEach
    void setUp() {
        ontologyId = UUID.randomUUID().toString();
        flowName = "order-flow-" + UUID.randomUUID().toString().substring(0, 8);
        epcStepPOMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<EpcStepPO>()
                        .ne("1", "1"));
    }

    @AfterEach
    void tearDown() {
        epcStepPOMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<EpcStepPO>()
                        .ne("1", "1"));
    }

    // ============================================================
    // 用例 1：save + findById 双向
    // ============================================================
    @Test
    @DisplayName("save + findById：插入后能按主键读回 Entity，字段一致")
    void save_andFindById_roundTrip() {
        EpcStep input = EpcStep.create(
                ontologyId, flowName, 1,
                "action-validate", 30000);

        EpcStep saved = repository.save(input);

        assertThat(saved.getId()).isEqualTo(input.getId());
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Optional<EpcStep> loaded = repository.findById(saved.getId());
        assertThat(loaded).isPresent();
        EpcStep reloaded = loaded.get();
        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getOntologyId()).isEqualTo(ontologyId);
        assertThat(reloaded.getFlowName()).isEqualTo(flowName);
        assertThat(reloaded.getStepOrder()).isEqualTo(1);
        assertThat(reloaded.getActionId()).isEqualTo("action-validate");
        assertThat(reloaded.getTimeoutMs()).isEqualTo(30000);
        assertThat(reloaded.getConditions()).isEqualTo("[]");
        assertThat(reloaded.getGuards()).isEqualTo("[]");
    }

    // ============================================================
    // 用例 2：findByOntologyId 按本体过滤
    // ============================================================
    @Test
    @DisplayName("findByOntologyId：按 ontologyId 过滤返回正确的 EpcStep 列表")
    void findByOntologyId_filters() {
        EpcStep s1 = newStep(flowName, 1, "action-a");
        EpcStep s2 = newStep(flowName, 2, "action-b");
        EpcStep s3 = EpcStep.create(
                UUID.randomUUID().toString(), "other-flow", 1,
                "action-c", 60000);

        repository.save(s1);
        repository.save(s2);
        repository.save(s3);

        List<EpcStep> result = repository.findByOntologyId(ontologyId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(EpcStep::getActionId)
                .containsExactlyInAnyOrder("action-a", "action-b");
        assertThat(result).extracting(EpcStep::getOntologyId).containsOnly(ontologyId);
    }

    // ============================================================
    // 用例 3：findByOntologyIdAndFlowName
    // ============================================================
    @Test
    @DisplayName("findByOntologyIdAndFlowName：按 ontologyId+flowName 组合过滤")
    void findByOntologyIdAndFlowName() {
        String otherFlow = "other-flow-" + UUID.randomUUID().toString().substring(0, 8);

        EpcStep s1 = newStep(flowName, 1, "action-1");
        EpcStep s2 = newStep(flowName, 2, "action-2");
        EpcStep s3 = newStep(otherFlow, 1, "action-other");

        repository.save(s1);
        repository.save(s2);
        repository.save(s3);

        List<EpcStep> result = repository.findByOntologyIdAndFlowName(ontologyId, flowName);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(EpcStep::getActionId)
                .containsExactlyInAnyOrder("action-1", "action-2");
    }

    // ============================================================
    // 用例 4：findByFlowNameOrderByStepOrder
    // ============================================================
    @Test
    @DisplayName("findByFlowNameOrderByStepOrder：按 flowName 过滤并按 stepOrder 升序返回")
    void findByFlowNameOrderByStepOrder() {
        EpcStep s1 = newStep(flowName, 3, "action-c");
        EpcStep s2 = newStep(flowName, 1, "action-a");
        EpcStep s3 = newStep(flowName, 2, "action-b");

        // 显式逆序插入
        repository.save(s1);
        sleep(5);
        repository.save(s2);
        sleep(5);
        repository.save(s3);

        List<EpcStep> result = repository.findByFlowNameOrderByStepOrder(flowName);

        assertThat(result).hasSize(3);
        assertThat(result).extracting(EpcStep::getStepOrder).containsExactly(1, 2, 3);
        assertThat(result).extracting(EpcStep::getActionId).containsExactly("action-a", "action-b", "action-c");
    }

    // ============================================================
    // 用例 5：save 的 upsert 语义（更新已存在的实体）
    // ============================================================
    @Test
    @DisplayName("save upsert：再次 save 同一 id 会更新已有记录")
    void save_upsert_updatesExisting() {
        EpcStep original = repository.save(newStep(flowName, 1, "action-original"));

        EpcStep updated = EpcStep.builder()
                .id(original.getId())
                .ontologyId(ontologyId)
                .flowName(flowName)
                .stepOrder(1)
                .actionId("action-updated")
                .conditions("[\"cond1\"]")
                .guards("[\"guard1\"]")
                .timeoutMs(99999)
                .createdAt(original.getCreatedAt())
                .updatedAt(original.getUpdatedAt())
                .build();

        repository.save(updated);

        Optional<EpcStep> loaded = repository.findById(original.getId());
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getActionId()).isEqualTo("action-updated");
        assertThat(loaded.get().getTimeoutMs()).isEqualTo(99999);
        assertThat(loaded.get().getConditions()).isEqualTo("[\"cond1\"]");
        assertThat(loaded.get().getGuards()).isEqualTo("[\"guard1\"]");
    }

    // ============================================================
    // 用例 6：deleteById（硬删除）
    // ============================================================
    @Test
    @DisplayName("deleteById：硬删除后 findById 返回空")
    void deleteById_hardDelete() {
        EpcStep saved = repository.save(newStep(flowName, 1, "action-to-delete"));

        repository.deleteById(saved.getId());

        Optional<EpcStep> loaded = repository.findById(saved.getId());
        assertThat(loaded).isEmpty();
    }

    // ============================================================
    // 用例 7：findByOntologyId 空结果
    // ============================================================
    @Test
    @DisplayName("findByOntologyId：无匹配时返回空列表")
    void findByOntologyId_empty() {
        List<EpcStep> result = repository.findByOntologyId(UUID.randomUUID().toString());
        assertThat(result).isEmpty();
    }

    // ============================================================
    // helpers
    // ============================================================

    private EpcStep newStep(String flow, int order, String actionId) {
        return EpcStep.create(ontologyId, flow, order, actionId, 60000);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
