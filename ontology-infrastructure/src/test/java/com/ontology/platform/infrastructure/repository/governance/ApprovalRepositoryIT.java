package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.ApprovalRequest;
import com.ontology.platform.domain.repository.governance.ApprovalRepository;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.ApprovalRequestConverter;
import com.ontology.platform.infrastructure.persistence.ApprovalRequestPO;
import com.ontology.platform.infrastructure.persistence.ApprovalRequestPOMapper;
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

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApprovalRepository IT")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class ApprovalRepositoryIT {

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
    @ComponentScan(basePackageClasses = {ApprovalRequestPOMapper.class, ApprovalRequestConverter.class, ApprovalRepositoryImpl.class},
            excludeFilters = {@ComponentScan.Filter(type = FilterType.REGEX,
                    pattern = {"com\\.ontology\\.platform\\.infrastructure\\.config\\.RedisConfig"})})
    @EnableTransactionManagement
    static class Config { @Bean public MyBatisPlusConfig c() { return new MyBatisPlusConfig(); } }

    @Autowired ApprovalRepository repo;
    @Autowired ApprovalRequestPOMapper mapper;

    @BeforeAll static void initSchema() { ItSchemaHelper.initV6GovernanceTables(PG); }

    @BeforeEach void setUp() { mapper.delete(new QueryWrapper<ApprovalRequestPO>().ne("1","1")); }
    @AfterEach void tearDown() { mapper.delete(new QueryWrapper<ApprovalRequestPO>().ne("1","1")); }

    @Test @DisplayName("save + findById")
    void saveAndFindById() {
        var a = ApprovalRequest.submit("agent-1", "action-1", "WRITE");
        repo.save(a);
        var found = repo.findById(a.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getAgentId()).isEqualTo("agent-1");
        assertThat(found.get().isPending()).isTrue();
    }

    @Test @DisplayName("findByAgentId")
    void findByAgentId() {
        repo.save(ApprovalRequest.submit("agent-a", "action-1", "READ"));
        repo.save(ApprovalRequest.submit("agent-a", "action-2", "WRITE"));
        repo.save(ApprovalRequest.submit("agent-b", "action-3", "READ"));
        assertThat(repo.findByAgentId("agent-a")).hasSize(2);
    }

    @Test @DisplayName("findPending filters non-pending")
    void findPending() {
        var a1 = ApprovalRequest.submit("agent-1", "action-1", "WRITE");
        var a2 = ApprovalRequest.submit("agent-2", "action-2", "READ");
        a2.approve("admin");
        repo.save(a1);
        repo.save(a2);
        assertThat(repo.findPending()).hasSize(1);
    }

    @Test @DisplayName("save upserts approved request")
    void saveUpserts() {
        var a = repo.save(ApprovalRequest.submit("agent-1", "action-1", "WRITE"));
        a.approve("admin");
        repo.save(a);
        var found = repo.findById(a.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isPending()).isFalse();
    }

    @Test @DisplayName("findById missing returns empty")
    void findByIdMissing() { assertThat(repo.findById("nonexistent")).isEmpty(); }
}
