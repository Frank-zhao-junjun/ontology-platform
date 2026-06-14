package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.DomainEvent;
import com.ontology.platform.domain.repository.DomainEventRepository;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.DomainEventConverter;
import com.ontology.platform.infrastructure.persistence.DomainEventPO;
import com.ontology.platform.infrastructure.persistence.DomainEventPOMapper;
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
 * DomainEventRepositoryImpl 的 Testcontainers 集成测试（IT 后缀以示与单元测试区分）。
 * <p>
 * 与 mock 单元测试的关系：
 * <ul>
 *   <li>单元测试：{@link DomainEventRepositoryImplTest} 覆盖契约/边界（不依赖 DB）。</li>
 *   <li>本 IT：在真实 PG（携带 AGE 扩展）容器中跑完整 CRUD/Mapper 链路，验证 SQL、列映射、约束。</li>
 * </ul>
 * <p>
 * 优雅降级：本机/CI 节点无 Docker 时，{@code @Testcontainers(disabledWithoutDocker = true)}
 * 会自动跳过整个测试类，并打印明确原因，不会污染构建结果。
 */
@DisplayName("DomainEventRepository 集成测试（Testcontainers PG+AGE）")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class DomainEventRepositoryIT {

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
                    DomainEventPOMapper.class,
                    DomainEventConverter.class,
                    DomainEventRepositoryImpl.class
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
    private DomainEventRepository repository;

    @Autowired
    private DomainEventPOMapper domainEventPOMapper;

    private String ontologyId;
    private String entityId;

    @BeforeEach
    void setUp() {
        ontologyId = UUID.randomUUID().toString();
        entityId = UUID.randomUUID().toString();
        domainEventPOMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DomainEventPO>()
                        .ne("1", "1"));
    }

    @AfterEach
    void tearDown() {
        domainEventPOMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DomainEventPO>()
                        .ne("1", "1"));
    }

    // ============================================================
    // 用例 1：save + findById 双向
    // ============================================================
    @Test
    @DisplayName("save + findById：插入后能按主键读回 Entity，字段一致")
    void save_andFindById_roundTrip() {
        DomainEvent input = DomainEvent.create(
                ontologyId, entityId,
                "entity.created", "Entity Created",
                "LIFECYCLE", "INFO");

        DomainEvent saved = repository.save(input);

        assertThat(saved.getId()).isEqualTo(input.getId());
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        Optional<DomainEvent> loaded = repository.findById(saved.getId());
        assertThat(loaded).isPresent();
        DomainEvent reloaded = loaded.get();
        assertThat(reloaded.getId()).isEqualTo(saved.getId());
        assertThat(reloaded.getOntologyId()).isEqualTo(ontologyId);
        assertThat(reloaded.getEntityId()).isEqualTo(entityId);
        assertThat(reloaded.getName()).isEqualTo("entity.created");
        assertThat(reloaded.getDisplayName()).isEqualTo("Entity Created");
        assertThat(reloaded.getEventType()).isEqualTo("LIFECYCLE");
        assertThat(reloaded.getSeverity()).isEqualTo("INFO");
        assertThat(reloaded.getPayloadSchema()).isEqualTo("{}");
        assertThat(reloaded.getDeleted()).isFalse();
    }

    // ============================================================
    // 用例 2：findByOntologyId 按本体过滤
    // ============================================================
    @Test
    @DisplayName("findByOntologyId：按 ontologyId 过滤返回正确的 DomainEvent 列表")
    void findByOntologyId_filters() {
        DomainEvent r1 = newEvent("evt.a", "Evt A", "LIFECYCLE");
        DomainEvent r2 = newEvent("evt.b", "Evt B", "BUSINESS");
        DomainEvent r3 = DomainEvent.create(
                UUID.randomUUID().toString(), entityId,
                "evt.c", "Evt C",
                "LIFECYCLE", "WARN");

        repository.save(r1);
        repository.save(r2);
        repository.save(r3);

        List<DomainEvent> result = repository.findByOntologyId(ontologyId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(DomainEvent::getName).containsExactlyInAnyOrder("evt.a", "evt.b");
        assertThat(result).extracting(DomainEvent::getOntologyId).containsOnly(ontologyId);
    }

    // ============================================================
    // 用例 3：findByOntologyIdAndEntityId
    // ============================================================
    @Test
    @DisplayName("findByOntologyIdAndEntityId：按 ontologyId+entityId 组合过滤")
    void findByOntologyIdAndEntityId() {
        String otherEntity = UUID.randomUUID().toString();

        DomainEvent e1 = newEvent("evt.x", "Evt X", "LIFECYCLE");
        DomainEvent e2 = DomainEvent.create(ontologyId, otherEntity, "evt.y", "Evt Y", "LIFECYCLE", "INFO");

        repository.save(e1);
        repository.save(e2);

        List<DomainEvent> result = repository.findByOntologyIdAndEntityId(ontologyId, entityId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("evt.x");
        assertThat(result.get(0).getEntityId()).isEqualTo(entityId);
    }

    // ============================================================
    // 用例 4：findByOntologyIdAndEventType
    // ============================================================
    @Test
    @DisplayName("findByOntologyIdAndEventType：按 ontologyId+eventType 组合过滤")
    void findByOntologyIdAndEventType() {
        DomainEvent e1 = newEvent("evt.p", "Evt P", "LIFECYCLE");
        DomainEvent e2 = newEvent("evt.q", "Evt Q", "BUSINESS");
        DomainEvent e3 = newEvent("evt.r", "Evt R", "LIFECYCLE");

        repository.save(e1);
        repository.save(e2);
        repository.save(e3);

        List<DomainEvent> result = repository.findByOntologyIdAndEventType(ontologyId, "LIFECYCLE");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(DomainEvent::getName).containsExactlyInAnyOrder("evt.p", "evt.r");
    }

    // ============================================================
    // 用例 5：save 的 upsert 语义（更新已存在的实体）
    // ============================================================
    @Test
    @DisplayName("save upsert：再次 save 同一 id 会更新已有记录")
    void save_upsert_updatesExisting() {
        DomainEvent original = repository.save(newEvent("upsert.test", "Before", "LIFECYCLE"));

        // 构造更新数据（同名 id，修改 displayName）
        DomainEvent updated = DomainEvent.builder()
                .id(original.getId())
                .ontologyId(ontologyId)
                .entityId(entityId)
                .name("upsert.test")
                .displayName("After Update")
                .eventType("LIFECYCLE")
                .severity("ERROR")
                .payloadSchema("{\"new\": true}")
                .deleted(false)
                .createdAt(original.getCreatedAt())
                .updatedAt(original.getUpdatedAt())
                .build();

        repository.save(updated);

        Optional<DomainEvent> loaded = repository.findById(original.getId());
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getDisplayName()).isEqualTo("After Update");
        assertThat(loaded.get().getSeverity()).isEqualTo("ERROR");
        assertThat(loaded.get().getPayloadSchema()).isEqualTo("{\"new\": true}");
        // updatedAt should have been refreshed by the save
        assertThat(loaded.get().getUpdatedAt()).isAfterOrEqualTo(original.getUpdatedAt());
    }

    // ============================================================
    // 用例 6：deleteById（软删除）
    // ============================================================
    @Test
    @DisplayName("deleteById：软删除后 deleted 标记为 true，findById 仍返回 Entity（含标记）")
    void deleteById_softDelete() {
        DomainEvent saved = repository.save(newEvent("to.delete", "To Delete", "LIFECYCLE"));

        repository.deleteById(saved.getId());

        // 软删除 — findById 仍然返回 Entity（deleted = true）
        Optional<DomainEvent> loaded = repository.findById(saved.getId());
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getDeleted()).isTrue();
    }

    // ============================================================
    // 用例 7：findById 不存在时返回空
    // ============================================================
    @Test
    @DisplayName("findById：不存在的 id 返回 Optional.empty")
    void findById_notFound_returnsEmpty() {
        Optional<DomainEvent> result = repository.findById(UUID.randomUUID().toString());
        assertThat(result).isEmpty();
    }

    // ============================================================
    // helpers
    // ============================================================

    private DomainEvent newEvent(String name, String displayName, String eventType) {
        return DomainEvent.create(ontologyId, entityId, name, displayName, eventType, "INFO");
    }
}
