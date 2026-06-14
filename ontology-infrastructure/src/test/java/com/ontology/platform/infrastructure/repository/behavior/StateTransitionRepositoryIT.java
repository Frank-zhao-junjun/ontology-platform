package com.ontology.platform.infrastructure.repository.behavior;

import com.ontology.platform.domain.entity.StateTransition;
import com.ontology.platform.domain.repository.behavior.StateTransitionRepository;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.StateTransitionConverter;
import com.ontology.platform.infrastructure.persistence.StateTransitionPO;
import com.ontology.platform.infrastructure.persistence.StateTransitionPOMapper;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StateTransitionRepository IT")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class StateTransitionRepositoryIT {

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
                    StateTransitionPOMapper.class,
                    StateTransitionConverter.class,
                    StateTransitionRepositoryImpl.class
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

    @Autowired StateTransitionRepository repo;
    @Autowired StateTransitionPOMapper mapper;

    @BeforeAll static void initSchema() { ItSchemaHelper.initV3BehaviorTables(PG); }

    @BeforeEach void setUp() {
        mapper.delete(new QueryWrapper<StateTransitionPO>().ne("1", "1"));
    }

    @AfterEach void tearDown() {
        mapper.delete(new QueryWrapper<StateTransitionPO>().ne("1", "1"));
    }

    @Test @DisplayName("save + findById round-trip")
    void saveAndFindById() {
        var smId = UUID.randomUUID().toString();
        var t = StateTransition.create(smId, "draft", "submitted", "submit_order", null);
        repo.save(t);
        var found = repo.findById(t.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTrigger()).isEqualTo("submit_order");
        assertThat(found.get().getFromState()).isEqualTo("draft");
        assertThat(found.get().getToState()).isEqualTo("submitted");
    }

    @Test @DisplayName("findByStateMachineId returns all transitions")
    void findByStateMachineId() {
        var smId = UUID.randomUUID().toString();
        repo.save(StateTransition.create(smId, "s1", "s2", "t1", null));
        repo.save(StateTransition.create(smId, "s2", "s3", "t2", "guard_x"));
        assertThat(repo.findByStateMachineId(smId)).hasSize(2);
    }

    @Test @DisplayName("save upserts existing record")
    void saveUpserts() {
        var smId = UUID.randomUUID().toString();
        var t = repo.save(StateTransition.create(smId, "s1", "s2", "t1", null));
        var updated = StateTransition.builder().id(t.getId()).stateMachineId(smId)
                .fromState("s1").toState("s3").trigger("t1_updated").build();
        repo.save(updated);
        var found = repo.findById(t.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getToState()).isEqualTo("s3");
    }

    @Test @DisplayName("deleteById hard-deletes")
    void deleteById() {
        var smId = UUID.randomUUID().toString();
        var t = repo.save(StateTransition.create(smId, "s1", "s2", "t1", null));
        repo.deleteById(t.getId());
        assertThat(repo.findById(t.getId())).isEmpty();
    }

    @Test @DisplayName("findById missing returns empty")
    void findByIdMissing() {
        assertThat(repo.findById("nonexistent")).isEmpty();
    }
}
