package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.common.enums.OntologyStatus;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.infrastructure.persistence.OntologyPO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OntologyConverter 单元测试
 */
@DisplayName("本体转换器测试")
class OntologyConverterTest {

    private OntologyConverter converter;
    private Ontology testOntology;
    private OntologyPO testOntologyPO;

    @BeforeEach
    void setUp() {
        converter = new OntologyConverter();
        
        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();

        testOntology = Ontology.builder()
                .id(id)
                .tenantId("default")
                .name("test_ontology")
                .displayName("测试本体")
                .description("这是一个测试本体")
                .version("0.1.0")
                .status(OntologyStatus.DRAFT)
                .objectTypeCount(3)
                .actionTypeCount(2)
                .createdBy("system")
                .createdAt(now)
                .updatedAt(now)
                .deleted(false)
                .build();

        testOntologyPO = OntologyPO.builder()
                .id(id)
                .tenantId("default")
                .name("test_ontology")
                .displayName("测试本体")
                .description("这是一个测试本体")
                .version("0.1.0")
                .status("draft")
                .publishedAt(null)
                .objectTypeCount(3)
                .actionTypeCount(2)
                .createdBy("system")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    @DisplayName("toEntity - 应该正确将PO转换为Entity")
    void toEntity_shouldConvertCorrectly() {
        // When
        Ontology entity = converter.toEntity(testOntologyPO);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(testOntologyPO.getId());
        assertThat(entity.getTenantId()).isEqualTo(testOntologyPO.getTenantId());
        assertThat(entity.getName()).isEqualTo(testOntologyPO.getName());
        assertThat(entity.getDisplayName()).isEqualTo(testOntologyPO.getDisplayName());
        assertThat(entity.getDescription()).isEqualTo(testOntologyPO.getDescription());
        assertThat(entity.getVersion()).isEqualTo(testOntologyPO.getVersion());
        assertThat(entity.getStatus()).isEqualTo(OntologyStatus.DRAFT);
        assertThat(entity.getObjectTypeCount()).isEqualTo(testOntologyPO.getObjectTypeCount());
        assertThat(entity.getActionTypeCount()).isEqualTo(testOntologyPO.getActionTypeCount());
        assertThat(entity.getCreatedBy()).isEqualTo(testOntologyPO.getCreatedBy());
        assertThat(entity.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("toEntity - 当PO为null时应该返回null")
    void toEntity_shouldReturnNullWhenPOIsNull() {
        // When
        Ontology entity = converter.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("toPO - 应该正确将Entity转换为PO")
    void toPO_shouldConvertCorrectly() {
        // When
        OntologyPO po = converter.toPO(testOntology);

        // Then
        assertThat(po).isNotNull();
        assertThat(po.getId()).isEqualTo(testOntology.getId());
        assertThat(po.getTenantId()).isEqualTo(testOntology.getTenantId());
        assertThat(po.getName()).isEqualTo(testOntology.getName());
        assertThat(po.getDisplayName()).isEqualTo(testOntology.getDisplayName());
        assertThat(po.getDescription()).isEqualTo(testOntology.getDescription());
        assertThat(po.getVersion()).isEqualTo(testOntology.getVersion());
        assertThat(po.getStatus()).isEqualTo("draft");
        assertThat(po.getObjectTypeCount()).isEqualTo(testOntology.getObjectTypeCount());
        assertThat(po.getActionTypeCount()).isEqualTo(testOntology.getActionTypeCount());
        assertThat(po.getCreatedBy()).isEqualTo(testOntology.getCreatedBy());
    }

    @Test
    @DisplayName("toPO - 当Entity为null时应该返回null")
    void toPO_shouldReturnNullWhenEntityIsNull() {
        // When
        OntologyPO po = converter.toPO(null);

        // Then
        assertThat(po).isNull();
    }

    @Test
    @DisplayName("toPO - 当Entity的status为null时应该使用默认值")
    void toPO_shouldUseDefaultStatusWhenEntityStatusIsNull() {
        // Given
        testOntology.setStatus(null);

        // When
        OntologyPO po = converter.toPO(testOntology);

        // Then
        assertThat(po.getStatus()).isEqualTo(OntologyStatus.DRAFT.getValue());
    }

    @Test
    @DisplayName("toEntityList - 应该正确转换PO列表")
    void toEntityList_shouldConvertListCorrectly() {
        // Given
        List<OntologyPO> poList = List.of(testOntologyPO);

        // When
        List<Ontology> entityList = converter.toEntityList(poList);

        // Then
        assertThat(entityList).hasSize(1);
        assertThat(entityList.get(0).getId()).isEqualTo(testOntologyPO.getId());
    }

    @Test
    @DisplayName("toEntityList - 当列表为null时应该返回空列表")
    void toEntityList_shouldReturnEmptyListWhenNull() {
        // When
        List<Ontology> entityList = converter.toEntityList(null);

        // Then
        assertThat(entityList).isEmpty();
    }

    @Test
    @DisplayName("toEntity - 应该正确转换PUBLISHED状态")
    void toEntity_shouldConvertPublishedStatus() {
        // Given
        testOntologyPO.setStatus("published");

        // When
        Ontology entity = converter.toEntity(testOntologyPO);

        // Then
        assertThat(entity.getStatus()).isEqualTo(OntologyStatus.PUBLISHED);
    }

    @Test
    @DisplayName("toEntity - 应该正确转换ARCHIVED状态")
    void toEntity_shouldConvertArchivedStatus() {
        // Given
        testOntologyPO.setStatus("archived");

        // When
        Ontology entity = converter.toEntity(testOntologyPO);

        // Then
        assertThat(entity.getStatus()).isEqualTo(OntologyStatus.ARCHIVED);
    }

    @Test
    @DisplayName("toPO - 应该正确处理publishedAt字段")
    void toPO_shouldHandlePublishedAtCorrectly() {
        // Given
        Instant publishedTime = Instant.now();
        testOntology.setPublishedAt(publishedTime);

        // When
        OntologyPO po = converter.toPO(testOntology);

        // Then
        assertThat(po.getPublishedAt()).isEqualTo(publishedTime);
    }
}
