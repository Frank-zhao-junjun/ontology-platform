package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.RolePermission;
import com.ontology.platform.domain.repository.governance.RolePermissionRepository;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.RolePermissionConverter;
import com.ontology.platform.infrastructure.persistence.RolePermissionPO;
import com.ontology.platform.infrastructure.persistence.RolePermissionPOMapper;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RolePermissionRepository IT")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class RolePermissionRepositoryIT {

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
    @ComponentScan(basePackageClasses = {RolePermissionPOMapper.class, RolePermissionConverter.class, RolePermissionRepositoryImpl.class},
            excludeFilters = {@ComponentScan.Filter(type = FilterType.REGEX,
                    pattern = {"com\\.ontology\\.platform\\.infrastructure\\.config\\.RedisConfig"})})
    @EnableTransactionManagement
    static class Config { @Bean public MyBatisPlusConfig c() { return new MyBatisPlusConfig(); } }

    @Autowired RolePermissionRepository repo;
    @Autowired RolePermissionPOMapper mapper;

    @BeforeAll static void initSchema() { ItSchemaHelper.initV6GovernanceTables(PG); }

    @BeforeEach void setUp() { mapper.delete(new QueryWrapper<RolePermissionPO>().ne("1","1")); }
    @AfterEach void tearDown() { mapper.delete(new QueryWrapper<RolePermissionPO>().ne("1","1")); }

    @Test @DisplayName("save + find via custom query")
    void saveAndQuery() {
        var rid = UUID.randomUUID().toString();
        repo.save(RolePermission.create(rid, "ontology", List.of("READ", "WRITE"), "sales"));
        assertThat(repo.findByRoleId(rid)).hasSize(1);
        assertThat(repo.findByDomain("sales")).hasSize(1);
    }

    @Test @DisplayName("findByDomain")
    void findByDomain() {
        repo.save(RolePermission.create(UUID.randomUUID().toString(), "res1", List.of("READ"), "sales"));
        repo.save(RolePermission.create(UUID.randomUUID().toString(), "res2", List.of("WRITE"), "sales"));
        repo.save(RolePermission.create(UUID.randomUUID().toString(), "res3", List.of("READ"), "finance"));
        assertThat(repo.findByDomain("sales")).hasSize(2);
        assertThat(repo.findByDomain("finance")).hasSize(1);
    }

    @Test @DisplayName("save with operations JSONB round-trip")
    void saveJsonbRoundTrip() {
        var p = RolePermission.create(UUID.randomUUID().toString(), "ontology", List.of("READ", "WRITE", "DELETE"), "admin");
        repo.save(p);
        var found = mapper.selectById(p.getId());
        assertThat(found).isNotNull();
        assertThat(found.getOperationsList()).containsExactly("READ", "WRITE", "DELETE");
    }
}
