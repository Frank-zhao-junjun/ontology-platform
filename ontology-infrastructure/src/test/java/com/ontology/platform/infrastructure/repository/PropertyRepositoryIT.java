package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.common.enums.PropertyDataType;
import com.ontology.platform.domain.repository.PropertyRepository;
import com.ontology.platform.domain.vo.Property;
import com.ontology.platform.infrastructure.config.MyBatisPlusConfig;
import com.ontology.platform.infrastructure.converter.PropertyConverter;
import com.ontology.platform.infrastructure.persistence.PropertyMapper;
import com.ontology.platform.infrastructure.persistence.PropertyPO;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PropertyRepository IT")
@TestPropertySource(properties = "spring.config.name=application-testcontainers")
@Testcontainers(disabledWithoutDocker = true)
class PropertyRepositoryIT {

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
                    PropertyMapper.class,
                    PropertyConverter.class,
                    PropertyRepositoryImpl.class
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
    private PropertyRepository repository;
    @Autowired
    private PropertyMapper mapper;
    private String objectTypeId;

    @BeforeEach
    void setUp() {
        objectTypeId = UUID.randomUUID().toString();
        mapper.delete(new QueryWrapper<PropertyPO>().ne("1", "1"));
    }

    @AfterEach
    void tearDown() {
        mapper.delete(new QueryWrapper<PropertyPO>().ne("1", "1"));
    }

    @Test
    @DisplayName("save+findById round-trip")
    void saveAndFindById() {
        Property input = Property.create(objectTypeId, "material_code", "物料编码", "desc", PropertyDataType.STRING, false);
        Property saved = repository.save(input);
        assertThat(saved.getId()).isEqualTo(input.getId());
        assertThat(saved.getName()).isEqualTo("material_code");
        Optional<Property> loaded = repository.findById(saved.getId());
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getObjectTypeId()).isEqualTo(objectTypeId);
    }

    @Test
    @DisplayName("findByObjectTypeId filters")
    void findByObjectTypeIdFilters() {
        String other = UUID.randomUUID().toString();
        repository.save(Property.create(objectTypeId, "prop_a", "A", "desc", PropertyDataType.STRING, false));
        repository.save(Property.create(objectTypeId, "prop_b", "B", "desc", PropertyDataType.STRING, false));
        repository.save(Property.create(other, "prop_c", "C", "desc", PropertyDataType.STRING, false));
        List<Property> result = repository.findByObjectTypeId(objectTypeId);
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Property::getName).contains("prop_a", "prop_b");
    }

    @Test
    @DisplayName("findByObjectTypeIdAndName exact")
    void findByObjectTypeIdAndNameExact() {
        repository.save(Property.create(objectTypeId, "unique_prop", "唯一", "desc", PropertyDataType.INTEGER, true));
        Optional<Property> found = repository.findByObjectTypeIdAndName(objectTypeId, "unique_prop");
        assertThat(found).isPresent();
        assertThat(found.get().isRequired()).isTrue();
    }

    @Test
    @DisplayName("existsByObjectTypeIdAndName")
    void existsByName() {
        assertThat(repository.existsByObjectTypeIdAndName(objectTypeId, "check_prop")).isFalse();
        repository.save(Property.create(objectTypeId, "check_prop", "检查", "desc", PropertyDataType.STRING, false));
        assertThat(repository.existsByObjectTypeIdAndName(objectTypeId, "check_prop")).isTrue();
    }

    @Test
    @DisplayName("countByObjectTypeId")
    void countByObjectTypeId() {
        assertThat(repository.countByObjectTypeId(objectTypeId)).isZero();
        repository.save(Property.create(objectTypeId, "c1", "C1", "desc", PropertyDataType.STRING, false));
        repository.save(Property.create(objectTypeId, "c2", "C2", "desc", PropertyDataType.STRING, false));
        assertThat(repository.countByObjectTypeId(objectTypeId)).isEqualTo(2L);
    }

    @Test
    @DisplayName("deleteById removes")
    void deleteByIdRemoves() {
        Property saved = repository.save(Property.create(objectTypeId, "to_del", "Del", "desc", PropertyDataType.STRING, false));
        assertThat(repository.findById(saved.getId())).isPresent();
        repository.deleteById(saved.getId());
        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
