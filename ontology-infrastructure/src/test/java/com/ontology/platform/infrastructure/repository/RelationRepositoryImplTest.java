package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.common.enums.RelationCardinality;
import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.infrastructure.converter.RelationConverter;
import com.ontology.platform.infrastructure.persistence.RelationPO;
import com.ontology.platform.infrastructure.persistence.RelationPOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * RelationRepositoryImpl 单元测试
 * 基于 MyBatis-Plus Mapper + Converter 的 mock 测试
 */
@DisplayName("RelationRepositoryImpl 测试")
class RelationRepositoryImplTest {

    private RelationRepositoryImpl repository;
    private RelationPOMapper relationPOMapper;
    private RelationConverter relationConverter;

    private static final String ONTOLOGY_ID = UUID.randomUUID().toString();
    private static final String SOURCE_TYPE_ID = UUID.randomUUID().toString();
    private static final String TARGET_TYPE_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        relationPOMapper = Mockito.mock(RelationPOMapper.class);
        // 使用真实 converter，避免大量 stub
        relationConverter = new RelationConverter();
        repository = new RelationRepositoryImpl(relationPOMapper, relationConverter);
    }

    private RelationPO buildPO(String id, String name) {
        return RelationPO.builder()
                .id(id)
                .ontologyId(ONTOLOGY_ID)
                .sourceTypeId(SOURCE_TYPE_ID)
                .targetTypeId(TARGET_TYPE_ID)
                .name(name)
                .displayName(name)
                .cardinality(RelationCardinality.ONE_TO_MANY.getValue())
                .build();
    }

    @Test
    @DisplayName("findById - 命中时返回 Entity")
    void findById_hit() {
        RelationPO po = buildPO(UUID.randomUUID().toString(), "owns");
        Mockito.when(relationPOMapper.selectById(po.getId())).thenReturn(po);

        Optional<Relation> result = repository.findById(po.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("owns");
        assertThat(result.get().getCardinality()).isEqualTo(RelationCardinality.ONE_TO_MANY);
    }

    @Test
    @DisplayName("findById - 未命中时返回空 Optional")
    void findById_miss() {
        Mockito.when(relationPOMapper.selectById(any())).thenReturn(null);
        assertThat(repository.findById("nope")).isEmpty();
    }

    @Test
    @DisplayName("findByOntologyId - 委托 mapper 并转换")
    void findByOntologyId() {
        List<RelationPO> poList = List.of(
                buildPO(UUID.randomUUID().toString(), "r1"),
                buildPO(UUID.randomUUID().toString(), "r2"));
        Mockito.when(relationPOMapper.selectByOntologyId(ONTOLOGY_ID)).thenReturn(poList);

        List<Relation> result = repository.findByOntologyId(ONTOLOGY_ID);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Relation::getName).containsExactly("r1", "r2");
    }

    @Test
    @DisplayName("findByOntologyId - 空集合")
    void findByOntologyId_empty() {
        Mockito.when(relationPOMapper.selectByOntologyId(ONTOLOGY_ID)).thenReturn(Collections.emptyList());
        assertThat(repository.findByOntologyId(ONTOLOGY_ID)).isEmpty();
    }

    @Test
    @DisplayName("existsByOntologyIdAndName - mapper 返回 >0 为 true")
    void existsByOntologyIdAndName() {
        Mockito.when(relationPOMapper.countByOntologyIdAndName(ONTOLOGY_ID, "owns")).thenReturn(1);
        Mockito.when(relationPOMapper.countByOntologyIdAndName(ONTOLOGY_ID, "missing")).thenReturn(0);

        assertThat(repository.existsByOntologyIdAndName(ONTOLOGY_ID, "owns")).isTrue();
        assertThat(repository.existsByOntologyIdAndName(ONTOLOGY_ID, "missing")).isFalse();
    }

    @Test
    @DisplayName("existsByOntologyIdAndNameAndIdNot - 委托 mapper")
    void existsByOntologyIdAndNameAndIdNot() {
        String excludeId = UUID.randomUUID().toString();
        Mockito.when(relationPOMapper.countByOntologyIdAndNameExcludingId(ONTOLOGY_ID, "owns", excludeId))
                .thenReturn(1);
        assertThat(repository.existsByOntologyIdAndNameAndIdNot(ONTOLOGY_ID, "owns", excludeId)).isTrue();
    }

    @Test
    @DisplayName("countByOntologyId - 直接返回 mapper 结果")
    void countByOntologyId() {
        Mockito.when(relationPOMapper.countByOntologyId(ONTOLOGY_ID)).thenReturn(7L);
        assertThat(repository.countByOntologyId(ONTOLOGY_ID)).isEqualTo(7L);
    }

    @Test
    @DisplayName("save - 写入 mapper 并填充 createdAt/updatedAt")
    void save_insertsAndStamps() {
        Relation relation = Relation.create(
                ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                "owns", "Owns", "desc", RelationCardinality.ONE_TO_MANY);
        relation.setCreatedAt(null);

        Relation saved = repository.save(relation);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        Mockito.verify(relationPOMapper).insert(any(RelationPO.class));
    }

    @Test
    @DisplayName("update - 存在时写入，不存在时抛 IllegalStateException")
    void update_existingOrMissing() {
        Relation relation = Relation.create(
                ONTOLOGY_ID, SOURCE_TYPE_ID, TARGET_TYPE_ID,
                "owns", "Owns", "desc", RelationCardinality.ONE_TO_MANY);
        String id = relation.getId();
        RelationPO existing = buildPO(id, "owns");
        Mockito.when(relationPOMapper.selectById(id)).thenReturn(existing);

        repository.update(relation);
        Mockito.verify(relationPOMapper).updateById(any(RelationPO.class));

        Mockito.when(relationPOMapper.selectById(id)).thenReturn(null);
        assertThatThrownBy(() -> repository.update(relation))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Relation not found");
    }

    @Test
    @DisplayName("deleteById - 委托 mapper")
    void deleteById() {
        String id = UUID.randomUUID().toString();
        repository.deleteById(id);
        Mockito.verify(relationPOMapper).deleteById(eq(id));
    }
}
