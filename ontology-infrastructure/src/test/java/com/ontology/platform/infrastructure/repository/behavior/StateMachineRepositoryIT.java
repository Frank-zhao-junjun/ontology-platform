package com.ontology.platform.infrastructure.repository.behavior;

import com.ontology.platform.domain.entity.StateMachine;
import com.ontology.platform.domain.repository.behavior.StateMachineRepository;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.StateMachineConverter;
import com.ontology.platform.infrastructure.persistence.StateMachinePO;
import com.ontology.platform.infrastructure.persistence.StateMachinePOMapper;
import com.ontology.platform.infrastructure.testsupport.ItSchemaHelper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;
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

@DisplayName("StateMachineRepository IT")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class StateMachineRepositoryIT {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>(
            DockerImageName.parse("apache/age")
                    .asCompatibleSubstituteFor("postgres")
                    .withTag("PG15_latest"))
            .withDatabaseName("ontology_it")
            .withUsername("ontology_it")
            .withPassword("ontology_it")
            .withReuse(true)
            .withStartupTimeoutSeconds(180);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", PG::getJdbcUrl);
        r.add("spring.datasource.username", PG::getUsername);
        r.add("spring.datasource.password", PG::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @SpringBootConfiguration
    @ImportAutoConfiguration({
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class
    })
    @ComponentScan(
            basePackageClasses = {
                    StateMachinePOMapper.class,
                    StateMachineConverter.class,
                    StateMachineRepositoryImpl.class
            },
            excludeFilters = {
                    @ComponentScan.Filter(
                            type = FilterType.REGEX,
                            pattern = {
                                    "com\\.ontology\\.platform\\.infrastructure\\.config\\.RedisConfig"
                            }
                    )
            }
    )
    @EnableTransactionManagement
    static class Config {
        @Bean
        public MyBatisPlusConfig myBatisPlusConfig() {
            return new MyBatisPlusConfig();
        }
    }

    @Autowired StateMachineRepository repo;
    @Autowired StateMachinePOMapper mapper;

    @BeforeAll static void initSchema() { ItSchemaHelper.initV3BehaviorTables(PG); }

    @BeforeEach void setUp() {
        mapper.delete(new QueryWrapper<StateMachinePO>().ne("1", "1"));
    }

    @AfterEach void tearDown() {
        mapper.delete(new QueryWrapper<StateMachinePO>().ne("1", "1"));
    }

    @Test @DisplayName("save + findById round-trip")
    void saveAndFindById() {
        var oid = UUID.randomUUID().toString();
        var sm = StateMachine.create(oid, "Order", "order_lifecycle", "created", "[\"created\",\"paid\",\"shipped\"]");
        repo.save(sm);

        var found = repo.findById(sm.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("order_lifecycle");
        assertThat(found.get().getInitialState()).isEqualTo("created");
        assertThat(found.get().getOntologyId()).isEqualTo(oid);
        assertThat(found.get().getEntityId()).isEqualTo("Order");
        assertThat(found.get().getStates()).isEqualTo("[\"created\",\"paid\",\"shipped\"]");
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
        assertThat(found.get().getDeleted()).isFalse();
    }

    @Test @DisplayName("findByOntologyId filters and cross-ontology isolation")
    void findByOntologyId() {
        var oid = UUID.randomUUID().toString();
        repo.save(StateMachine.create(oid, "E1", "sm1", "s1", "[\"s1\",\"s2\"]"));
        repo.save(StateMachine.create(oid, "E2", "sm2", "s1", "[\"s1\",\"s3\"]"));
        repo.save(StateMachine.create(UUID.randomUUID().toString(), "E3", "sm3", "s1", "[]"));

        List<StateMachine> result = repo.findByOntologyId(oid);
        assertThat(result).hasSize(2);
        assertThat(result).extracting(StateMachine::getName).containsExactlyInAnyOrder("sm1", "sm2");
    }

    @Test @DisplayName("findByOntologyIdAndEntityId filters correctly")
    void findByOntologyIdAndEntityId() {
        var oid = UUID.randomUUID().toString();
        repo.save(StateMachine.create(oid, "EntityA", "sm_a", "init", "[]"));
        repo.save(StateMachine.create(oid, "EntityB", "sm_b", "init", "[]"));
        repo.save(StateMachine.create(oid, "EntityA", "sm_a2", "init", "[]"));

        List<StateMachine> result = repo.findByOntologyIdAndEntityId(oid, "EntityA");
        assertThat(result).hasSize(2);
        assertThat(result).extracting(StateMachine::getName).containsExactlyInAnyOrder("sm_a", "sm_a2");
    }

    @Test @DisplayName("save upserts existing record")
    void saveUpserts() {
        var oid = UUID.randomUUID().toString();
        var sm = repo.save(StateMachine.create(oid, "E1", "upsert_sm", "draft", "[\"draft\"]"));

        var updated = StateMachine.builder()
                .id(sm.getId())
                .ontologyId(oid)
                .entityId("E1")
                .name("upsert_sm_renamed")
                .initialState("submitted")
                .states("[\"draft\",\"submitted\"]")
                .build();
        repo.save(updated);

        var found = repo.findById(sm.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("upsert_sm_renamed");
        assertThat(found.get().getInitialState()).isEqualTo("submitted");
        assertThat(found.get().getStates()).isEqualTo("[\"draft\",\"submitted\"]");
    }

    @Test @DisplayName("deleteById soft-deletes")
    void deleteById() {
        var oid = UUID.randomUUID().toString();
        var sm = repo.save(StateMachine.create(oid, "E1", "delete_me", "s1", "[]"));

        repo.deleteById(sm.getId());

        var po = mapper.selectById(sm.getId());
        assertThat(po.getDeleted()).isTrue();
    }

    @Test @DisplayName("findById missing returns empty")
    void findByIdMissing() {
        Optional<StateMachine> result = repo.findById("nonexistent");
        assertThat(result).isEmpty();
    }

    @Test @DisplayName("soft-deleted record still queryable via findById")
    void softDeletedRecordStillPresent() {
        var sm = repo.save(StateMachine.create(UUID.randomUUID().toString(), "E1", "soft_del", "s1", "[]"));
        repo.deleteById(sm.getId());

        var found = repo.findById(sm.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDeleted()).isTrue();
    }
}
