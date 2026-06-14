package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.Causality;
import com.ontology.platform.domain.repository.CausalityRepository;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.CausalityConverter;
import com.ontology.platform.infrastructure.persistence.CausalityPO;
import com.ontology.platform.infrastructure.persistence.CausalityPOMapper;
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
 * CausalityRepositoryImpl 的 Testcontainers 集成测试（IT 后缀以示与单元测试区分）。
 * <p>
 * 与 mock 单元测试的关系：
 * <ul>
 *   <li>单元测试：{@link CausalityRepositoryImplTest} 覆盖契约/边界（不依赖 DB）。</li>
 *   <li>本 IT：在真实 PG（携带 AGE 扩展）容器中跑完整 CRUD/Mapper 链路，验证 SQL、列映射、约束。</li>
 * </ul>
 * <p>
 * 优雅降级：本机/CI 节点无 Docker 时，{@code @Testcontainers(disabledWithoutDocker = true)}
 * 会自动跳过整个测试类，并打印明确原因，不会污染构建结果。
 */
@DisplayName("CausalityRepository 集成测试（Testcontainers PG+AGE）")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class CausalityRepositoryIT {

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
                    CausalityPOMapper.class,
                    CausalityConverter.class,
                    CausalityRepositoryImpl.class
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
    private CausalityRepository repository;

    @Autowired
    private CausalityPOMapper causalityPOMapper;

    private String ontologyId;
    private String causeEventId;
    private String effectEventId;

    @BeforeEach
    void setUp() {
        ontologyId = UUID.randomUUID().toString();
        causeEventId = UUID.randomUUID().toString();
        effectEventId = UUID.randomUUID().toString();
        causalityPOMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CausalityPO>()
                        .ne("1", "1"));
    }

    @AfterEach
    void tearDown() {
        causalityPOMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CausalityPO>()
                        .ne("1", "1"));
    }

    // ============================================================
    // 用例 1：save + findById 双向
    // ============================================================
    @Test
    @DisplayName("save + findById：插入后能按主键读回 Entity，字段一致")
    void save_andFindById_roundTrip() {
        Causality input = Causality.create(
                ontologyId, causeEventId, effectEventId,
                "事件A 导致 事件B");

        Causality saved = repository.save(input);

        assertThat(saved.getId()).isEqualTo(input.getId());
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<Causality> loaded = repository.findById(saved.getId());
        assertThat(loaded).isPresent();
        Causality reloaded = loaded.get();
        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getOntologyId()).isEqualTo(ontologyId);
        assertThat(reloaded.getCauseEventId()).isEqualTo(causeEventId);
        assertThat(reloaded.getEffectEventId()).isEqualTo(effectEventId);
        assertThat(reloaded.getDescription()).isEqualTo("事件A 导致 事件B");
        assertThat(reloaded.getDelayMs()).isZero();
    }

    // ============================================================
    // 用例 2：findByOntologyId 按本体过滤
    // ============================================================
    @Test
    @DisplayName("findByOntologyId：按 ontologyId 过滤返回正确的 Causality 列表")
    void findByOntologyId_filters() {
        Causality c1 = newCausality(causeEventId, effectEventId, "causality 1");
        Causality c2 = newCausality(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "causality 2");
        Causality c3 = Causality.create(
                UUID.randomUUID().toString(),
                causeEventId, effectEventId,
                "other ontology");

        repository.save(c1);
        repository.save(c2);
        repository.save(c3);

        List<Causality> result = repository.findByOntologyId(ontologyId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Causality::getDescription)
                .containsExactlyInAnyOrder("causality 1", "causality 2");
        assertThat(result).extracting(Causality::getOntologyId).containsOnly(ontologyId);
    }

    // ============================================================
    // 用例 3：findByCauseEventId
    // ============================================================
    @Test
    @DisplayName("findByCauseEventId：按 causeEventId 过滤")
    void findByCauseEventId() {
        Causality c1 = newCausality(causeEventId, effectEventId, "cause->effect");
        Causality c2 = newCausality(causeEventId, UUID.randomUUID().toString(), "cause->other");
        Causality c3 = newCausality(UUID.randomUUID().toString(), effectEventId, "other->effect");

        repository.save(c1);
        repository.save(c2);
        repository.save(c3);

        List<Causality> result = repository.findByCauseEventId(causeEventId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Causality::getEffectEventId)
                .containsExactlyInAnyOrder(effectEventId, c2.getEffectEventId());
    }

    // ============================================================
    // 用例 4：findByEffectEventId
    // ============================================================
    @Test
    @DisplayName("findByEffectEventId：按 effectEventId 过滤")
    void findByEffectEventId() {
        Causality c1 = newCausality(causeEventId, effectEventId, "cause->effect");
        Causality c2 = newCausality(UUID.randomUUID().toString(), effectEventId, "other->effect");
        Causality c3 = newCausality(causeEventId, UUID.randomUUID().toString(), "cause->other");

        repository.save(c1);
        repository.save(c2);
        repository.save(c3);

        List<Causality> result = repository.findByEffectEventId(effectEventId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Causality::getCauseEventId)
                .containsExactlyInAnyOrder(causeEventId, c2.getCauseEventId());
    }

    // ============================================================
    // 用例 5：save 的 upsert 语义（更新已存在的实体）
    // ============================================================
    @Test
    @DisplayName("save upsert：再次 save 同一 id 会更新已有记录")
    void save_upsert_updatesExisting() {
        Causality original = repository.save(newCausality(causeEventId, effectEventId, "original"));

        Causality updated = Causality.builder()
                .id(original.getId())
                .ontologyId(ontologyId)
                .causeEventId(causeEventId)
                .effectEventId(effectEventId)
                .description("updated description")
                .delayMs(5000)
                .createdAt(original.getCreatedAt())
                .build();

        repository.save(updated);

        Optional<Causality> loaded = repository.findById(original.getId());
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getDescription()).isEqualTo("updated description");
        assertThat(loaded.get().getDelayMs()).isEqualTo(5000);
    }

    // ============================================================
    // 用例 6：deleteById（硬删除）
    // ============================================================
    @Test
    @DisplayName("deleteById：硬删除后 findById 返回空")
    void deleteById_hardDelete() {
        Causality saved = repository.save(newCausality(causeEventId, effectEventId, "to delete"));

        repository.deleteById(saved.getId());

        Optional<Causality> loaded = repository.findById(saved.getId());
        assertThat(loaded).isEmpty();
    }

    // ============================================================
    // 用例 7：findById 不存在时返回空
    // ============================================================
    @Test
    @DisplayName("findById：不存在的 id 返回 Optional.empty")
    void findById_notFound_returnsEmpty() {
        Optional<Causality> result = repository.findById(UUID.randomUUID().toString());
        assertThat(result).isEmpty();
    }

    // ============================================================
    // helpers
    // ============================================================

    private Causality newCausality(String causeId, String effectId, String description) {
        return Causality.create(ontologyId, causeId, effectId, description);
    }
}
