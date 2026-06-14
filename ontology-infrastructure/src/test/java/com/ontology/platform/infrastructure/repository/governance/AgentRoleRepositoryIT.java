package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.AgentRole;
import com.ontology.platform.domain.repository.governance.AgentRoleRepository;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.AgentRoleConverter;
import com.ontology.platform.infrastructure.persistence.AgentRolePO;
import com.ontology.platform.infrastructure.persistence.AgentRolePOMapper;
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

@DisplayName("AgentRoleRepository IT")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class AgentRoleRepositoryIT {

    @Container @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> PG = new PostgreSQLContainer<>(
            DockerImageName.parse("apache/age").asCompatibleSubstituteFor("postgres").withTag("PG15_latest"))
            .withDatabaseName("ontology_it").withUsername("ontology_it").withPassword("ontology_it")
            .withReuse(true).withStartupTimeoutSeconds(180);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", PG::getJdbcUrl);
        r.add("spring.datasource.username", PG::getUsername);
        r.add("spring.datasource.password", PG::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @SpringBootConfiguration
    @ImportAutoConfiguration({DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class})
    @ComponentScan(basePackageClasses = {AgentRolePOMapper.class, AgentRoleConverter.class, AgentRoleRepositoryImpl.class},
            excludeFilters = {@ComponentScan.Filter(type = FilterType.REGEX,
                    pattern = {"com\\.ontology\\.platform\\.infrastructure\\.config\\.RedisConfig"})})
    @EnableTransactionManagement
    static class Config { @Bean public MyBatisPlusConfig c() { return new MyBatisPlusConfig(); } }

    @Autowired AgentRoleRepository repo;
    @Autowired AgentRolePOMapper mapper;

    @BeforeAll static void initSchema() { ItSchemaHelper.initV6GovernanceTables(PG); }

    @BeforeEach void setUp() { mapper.delete(new QueryWrapper<AgentRolePO>().ne("1","1")); }
    @AfterEach void tearDown() { mapper.delete(new QueryWrapper<AgentRolePO>().ne("1","1")); }

    @Test @DisplayName("save + findById round-trip")
    void saveAndFindById() {
        var r = AgentRole.create(UUID.randomUUID().toString(), "sales", "admin");
        repo.save(r);
        var found = repo.findById(r.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDomain()).isEqualTo("sales");
        assertThat(found.get().getRole()).isEqualTo("admin");
    }

    @Test @DisplayName("findByTokenId")
    void findByTokenId() {
        var tid = UUID.randomUUID().toString();
        repo.save(AgentRole.create(tid, "sales", "admin"));
        repo.save(AgentRole.create(tid, "finance", "viewer"));
        repo.save(AgentRole.create(UUID.randomUUID().toString(), "hr", "admin"));
        assertThat(repo.findByTokenId(tid)).hasSize(2);
    }

    @Test @DisplayName("findByDomain")
    void findByDomain() {
        repo.save(AgentRole.create(UUID.randomUUID().toString(), "sales", "admin"));
        repo.save(AgentRole.create(UUID.randomUUID().toString(), "sales", "viewer"));
        repo.save(AgentRole.create(UUID.randomUUID().toString(), "finance", "admin"));
        assertThat(repo.findByDomain("sales")).hasSize(2);
    }

    @Test @DisplayName("findById missing returns empty")
    void findByIdMissing() { assertThat(repo.findById("nonexistent")).isEmpty(); }
}
