package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.common.enums.RelationCardinality;
import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.domain.repository.RelationRepository;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.RelationConverter;
import com.ontology.platform.infrastructure.persistence.RelationPO;
import com.ontology.platform.infrastructure.persistence.RelationPOMapper;
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
 * RelationRepositoryImpl 的 Testcontainers 集成测试（IT 后缀以示与单元测试区分）。
 * <p>
 * 与 mock 单元测试的关系：
 * <ul>
 *   <li>单元测试：{@link RelationRepositoryImplTest} 覆盖契约/边界（不依赖 DB）。</li>
 *   <li>本 IT：在真实 PG（携带 AGE 扩展）容器中跑完整 CRUD/Mapper 链路，验证 SQL、列映射、约束。</li>
 * </ul>
 * <p>
 * 优雅降级：本机/CI 节点无 Docker 时，{@code @Testcontainers(disabledWithoutDocker = true)}
 * 会自动跳过整个测试类，并打印明确原因，不会污染构建结果。
 */
@DisplayName("RelationRepository 集成测试（Testcontainers PG+AGE）")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class RelationRepositoryIT {

    /**
     * 单例 PG+AGE 容器：{@code @Container} 配合 {@code @Testcontainers} 扩展：
     * <ul>
     *   <li>JVM 内单例，所有用例共享一个容器实例（{@code static} + {@code @Container}）。</li>
     *   <li>无 Docker 时扩展自动 disable 整个类，跳过而非失败。</li>
     * </ul>
     */
    @Container
    @SuppressWarnings("resource") // 由 @Testcontainers 扩展负责生命周期
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
        // 容器启动后用 JDBC URL 覆盖 application-testcontainers.yml 中的占位
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
                    RelationPOMapper.class,
                    RelationConverter.class,
                    RelationRepositoryImpl.class
            },
            // 排除会拉起 Redis/AGE 业务 Bean 的重型配置
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
        // 显式引入 MyBatis-Plus 扫描，使 RelationPOMapper 进入 SqlSession
        @org.springframework.context.annotation.Bean
        public MyBatisPlusConfig myBatisPlusConfig() {
            return new MyBatisPlusConfig();
        }
    }

    @Autowired
    private RelationRepository repository;

    @Autowired
    private RelationPOMapper relationPOMapper;

    private String ontologyId;
    private String sourceTypeId;
    private String targetTypeId;

    @BeforeEach
    void setUp() {
        ontologyId = UUID.randomUUID().toString();
        sourceTypeId = UUID.randomUUID().toString();
        targetTypeId = UUID.randomUUID().toString();
        // 每次用例前清表，保证用例间隔离
        relationPOMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RelationPO>()
                        .ne("1", "1"));
    }

    @AfterEach
    void tearDown() {
        relationPOMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<RelationPO>()
                        .ne("1", "1"));
    }

    // ============================================================
    // 用例 1：save + findById 双向
    // ============================================================
    @Test
    @DisplayName("save + findById：插入后能按主键读回 Entity，字段一致")
    void save_andFindById_roundTrip() {
        Relation input = Relation.create(
                ontologyId, sourceTypeId, targetTypeId,
                "owns", "Owns", "持有关系",
                RelationCardinality.ONE_TO_MANY);
        input.setReverse("owned_by", "被持有");

        Relation saved = repository.save(input);

        assertThat(saved.getId()).isEqualTo(input.getId());
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Optional<Relation> loaded = repository.findById(saved.getId());
        assertThat(loaded).isPresent();
        Relation reloaded = loaded.get();
        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getOntologyId()).isEqualTo(ontologyId);
        assertThat(reloaded.getName()).isEqualTo("owns");
        assertThat(reloaded.getDisplayName()).isEqualTo("Owns");
        assertThat(reloaded.getDescription()).isEqualTo("持有关系");
        assertThat(reloaded.getCardinality()).isEqualTo(RelationCardinality.ONE_TO_MANY);
        assertThat(reloaded.getReverseName()).isEqualTo("owned_by");
        assertThat(reloaded.getReverseDisplayName()).isEqualTo("被持有");
    }

    // ============================================================
    // 用例 2：findByOntologyId 按本体过滤
    // ============================================================
    @Test
    @DisplayName("findByOntologyId：按 ontologyId 过滤并按创建时间升序返回")
    void findByOntologyId_filtersAndOrders() {
        // 准备：同 ontology 下 2 条 + 另一个 ontology 下 1 条
        Relation r1 = newRelation("rel_a", "Rel A");
        Relation r2 = newRelation("rel_b", "Rel B");
        Relation r3 = Relation.create(
                UUID.randomUUID().toString(),
                sourceTypeId, targetTypeId,
                "rel_c", "Rel C", null,
                RelationCardinality.ONE_TO_ONE);

        // 显式顺序：先 r1 后 r2，验证 ORDER BY created_at ASC
        repository.save(r1);
        sleep(5);
        repository.save(r2);
        repository.save(r3);

        List<Relation> result = repository.findByOntologyId(ontologyId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Relation::getName).containsExactly("rel_a", "rel_b");
        // 不应包含另一个 ontology 的数据
        assertThat(result).extracting(Relation::getOntologyId)
                .containsOnly(ontologyId);
    }

    // ============================================================
    // 用例 3：existsByOntologyIdAndName
    // ============================================================
    @Test
    @DisplayName("existsByOntologyIdAndName：存在/不存在场景双向断言")
    void existsByOntologyIdAndName() {
        Relation saved = repository.save(newRelation("dup_check", "Dup Check"));

        assertThat(repository.existsByOntologyIdAndName(ontologyId, "dup_check")).isTrue();
        assertThat(repository.existsByOntologyIdAndName(ontologyId, "missing")).isFalse();
        // 隔离：另一个 ontology 下同名也不应误报
        assertThat(repository.existsByOntologyIdAndName(
                UUID.randomUUID().toString(), "dup_check")).isFalse();
        // sanity：saved 的 id 是真实存在的（间接覆盖 findById 路径）
        assertThat(saved.getId()).isNotBlank();
    }

    // ============================================================
    // 用例 4：countByOntologyId
    // ============================================================
    @Test
    @DisplayName("countByOntologyId：插入 N 条后统计等于 N，跨 ontology 隔离")
    void countByOntologyId() {
        assertThat(repository.countByOntologyId(ontologyId)).isZero();

        repository.save(newRelation("c1", "C1"));
        repository.save(newRelation("c2", "C2"));
        repository.save(newRelation("c3", "C3"));
        assertThat(repository.countByOntologyId(ontologyId)).isEqualTo(3L);

        // 另一个 ontology 不应被计入
        String otherOntology = UUID.randomUUID().toString();
        repository.save(Relation.create(
                otherOntology, sourceTypeId, targetTypeId,
                "c4", "C4", null, RelationCardinality.ONE_TO_ONE));
        assertThat(repository.countByOntologyId(ontologyId)).isEqualTo(3L);
        assertThat(repository.countByOntologyId(otherOntology)).isEqualTo(1L);
    }

    // ============================================================
    // helpers
    // ============================================================

    private Relation newRelation(String name, String displayName) {
        return Relation.create(
                ontologyId, sourceTypeId, targetTypeId,
                name, displayName, "desc-" + name,
                RelationCardinality.ONE_TO_MANY);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
