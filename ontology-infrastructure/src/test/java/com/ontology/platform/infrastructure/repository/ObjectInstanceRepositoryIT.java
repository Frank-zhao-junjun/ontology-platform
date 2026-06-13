package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.ObjectInstance;
import com.ontology.platform.domain.repository.ObjectInstanceRepository;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.ObjectInstanceConverter;
import com.ontology.platform.infrastructure.persistence.ObjectInstancePO;
import com.ontology.platform.infrastructure.persistence.ObjectInstancePOMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ObjectInstanceRepository IT")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class ObjectInstanceRepositoryIT {

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
                    ObjectInstancePOMapper.class,
                    ObjectInstanceConverter.class,
                    ObjectInstanceRepositoryImpl.class
            },
            excludeFilters = {
                    @ComponentScan.Filter(
                            type = FilterType.REGEX,
                            pattern = "com\\.ontology\\.platform\\.infrastructure\\.config\\.RedisConfig"
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
    private ObjectInstanceRepository repository;
    @Autowired
    private ObjectInstancePOMapper mapper;
    private String objectTypeId;

    @BeforeEach
    void setUp() {
        objectTypeId = UUID.randomUUID().toString();
        mapper.delete(new QueryWrapper<ObjectInstancePO>().ne("1", "1"));
    }

    @AfterEach
    void tearDown() {
        mapper.delete(new QueryWrapper<ObjectInstancePO>().ne("1", "1"));
    }

    @Test
    @DisplayName("save+findById round-trip")
    void saveAndFindById() {
        ObjectInstance input = ObjectInstance.create(
                UUID.randomUUID().toString(), objectTypeId, "PK-001",
                Map.of("name", "test", "status", "active"));
        input.setExtendedData(Map.of("ext", "extra"));
        ObjectInstance saved = repository.save(input);
        assertThat(saved.getId()).isEqualTo(input.getId());
        Optional<ObjectInstance> loaded = repository.findById(saved.getId());
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getObjectTypeId()).isEqualTo(objectTypeId);
    }

    @Test
    @DisplayName("findByObjectTypeId pagination")
    void findByObjectTypeIdPaginates() {
        String other = UUID.randomUUID().toString();
        repository.save(ObjectInstance.create(UUID.randomUUID().toString(), objectTypeId, "PK-A", Map.of("seq", 1)));
        repository.save(ObjectInstance.create(UUID.randomUUID().toString(), objectTypeId, "PK-B", Map.of("seq", 2)));
        repository.save(ObjectInstance.create(UUID.randomUUID().toString(), other, "PK-C", Map.of("seq", 3)));
        List<ObjectInstance> page = repository.findByObjectTypeId(objectTypeId, 0, 2);
        assertThat(page).hasSize(2);
        assertThat(page).allMatch(i -> objectTypeId.equals(i.getObjectTypeId()));
    }

    @Test
    @DisplayName("countByObjectTypeId isolation")
    void countIsolated() {
        assertThat(repository.countByObjectTypeId(objectTypeId)).isZero();
        String other = UUID.randomUUID().toString();
        repository.save(ObjectInstance.create(UUID.randomUUID().toString(), objectTypeId, "PK-1", null));
        repository.save(ObjectInstance.create(UUID.randomUUID().toString(), objectTypeId, "PK-2", null));
        repository.save(ObjectInstance.create(UUID.randomUUID().toString(), other, "PK-3", null));
        assertThat(repository.countByObjectTypeId(objectTypeId)).isEqualTo(2L);
    }

    @Test
    @DisplayName("existsByPk bidirectional")
    void existsByPk() {
        assertThat(repository.existsByObjectTypeIdAndPrimaryKeyValue(objectTypeId, "PK-X")).isFalse();
        repository.save(ObjectInstance.create(UUID.randomUUID().toString(), objectTypeId, "PK-X", null));
        assertThat(repository.existsByObjectTypeIdAndPrimaryKeyValue(objectTypeId, "PK-X")).isTrue();
    }

    @Test
    @DisplayName("deleteById removes")
    void deleteByIdRemoves() {
        ObjectInstance saved = repository.save(ObjectInstance.create(
                UUID.randomUUID().toString(), objectTypeId, "PK-DEL", null));
        assertThat(repository.findById(saved.getId())).isPresent();
        repository.deleteById(saved.getId());
        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
