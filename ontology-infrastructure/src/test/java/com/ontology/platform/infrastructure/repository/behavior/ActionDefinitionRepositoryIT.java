package com.ontology.platform.infrastructure.repository.behavior;

import com.ontology.platform.domain.entity.ActionDefinition;
import com.ontology.platform.domain.repository.behavior.ActionDefinitionRepository;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.ActionDefinitionConverter;
import com.ontology.platform.infrastructure.persistence.ActionDefinitionPO;
import com.ontology.platform.infrastructure.persistence.ActionDefinitionPOMapper;
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

@DisplayName("ActionDefinitionRepository IT")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class ActionDefinitionRepositoryIT {

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
                    ActionDefinitionPOMapper.class,
                    ActionDefinitionConverter.class,
                    ActionDefinitionRepositoryImpl.class
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

    @Autowired ActionDefinitionRepository repo;
    @Autowired ActionDefinitionPOMapper mapper;

    @BeforeAll static void initSchema() { ItSchemaHelper.initV3BehaviorTables(PG); }

    @BeforeEach void setUp() {
        mapper.delete(new QueryWrapper<ActionDefinitionPO>().ne("1", "1"));
    }

    @AfterEach void tearDown() {
        mapper.delete(new QueryWrapper<ActionDefinitionPO>().ne("1", "1"));
    }

    @Test @DisplayName("save + findById round-trip")
    void saveAndFindById() {
        var a = ActionDefinition.create(UUID.randomUUID().toString(), "Order",
                "create_order", "Create Order", "CREATE", "sales", "WRITE");
        repo.save(a);
        var found = repo.findById(a.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("create_order");
        assertThat(found.get().getRiskLevel()).isEqualTo("WRITE");
    }

    @Test @DisplayName("findByOntologyId filters and cross-ontology isolation")
    void findByOntologyId() {
        var oid = UUID.randomUUID().toString();
        repo.save(ActionDefinition.create(oid, "E1", "a1", "A1", "READ", "d1", "READ"));
        repo.save(ActionDefinition.create(oid, "E2", "a2", "A2", "READ", "d1", "READ"));
        repo.save(ActionDefinition.create(UUID.randomUUID().toString(), "E3", "a3", "A3", "READ", "d1", "READ"));
        assertThat(repo.findByOntologyId(oid)).hasSize(2);
    }

    @Test @DisplayName("findByOntologyIdAndDomain filters correctly")
    void findByOntologyIdAndDomain() {
        var oid = UUID.randomUUID().toString();
        repo.save(ActionDefinition.create(oid, "E1", "a1", "A1", "READ", "sales", "READ"));
        repo.save(ActionDefinition.create(oid, "E2", "a2", "A2", "READ", "finance", "READ"));
        assertThat(repo.findByOntologyIdAndDomain(oid, "sales")).hasSize(1);
    }

    @Test @DisplayName("deleteById soft-deletes")
    void deleteById() {
        var a = repo.save(ActionDefinition.create(UUID.randomUUID().toString(),
                "E1", "a1", "A1", "READ", "d1", "READ"));
        repo.deleteById(a.getId());
        assertThat(mapper.selectById(a.getId()).getDeleted()).isTrue();
    }

    @Test @DisplayName("findById missing returns empty")
    void findByIdMissing() {
        assertThat(repo.findById("nonexistent")).isEmpty();
    }
}
