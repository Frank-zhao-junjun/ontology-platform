package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.domain.entity.ObjectInstance;
import com.ontology.platform.infrastructure.converter.ObjectInstanceConverter;
import com.ontology.platform.infrastructure.persistence.ObjectInstancePO;
import com.ontology.platform.infrastructure.persistence.ObjectInstancePOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * ObjectInstanceRepositoryImpl 单元测试
 * 基于 MyBatis-Plus Mapper + Converter 的 mock 测试
 */
@DisplayName("ObjectInstanceRepositoryImpl 测试")
class ObjectInstanceRepositoryImplTest {

    private ObjectInstanceRepositoryImpl repository;
    private ObjectInstancePOMapper objectInstancePOMapper;
    private ObjectInstanceConverter objectInstanceConverter;

    private static final String ONTOLOGY_ID = UUID.randomUUID().toString();
    private static final String OBJECT_TYPE_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        objectInstancePOMapper = Mockito.mock(ObjectInstancePOMapper.class);
        // 使用真实 converter，避免大量 stub
        objectInstanceConverter = new ObjectInstanceConverter();
        repository = new ObjectInstanceRepositoryImpl(objectInstancePOMapper, objectInstanceConverter);
    }

    private ObjectInstancePO buildPO(String id, String primaryKeyValue) {
        ObjectInstancePO po = ObjectInstancePO.builder()
                .id(id)
                .ontologyId(ONTOLOGY_ID)
                .objectTypeId(OBJECT_TYPE_ID)
                .primaryKeyValue(primaryKeyValue)
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .build();
        po.setCoreDataMap(Map.of("name", "device-" + primaryKeyValue, "status", "active"));
        po.setExtendedDataMap(Map.of("tags", List.of("a", "b")));
        return po;
    }

    @Test
    @DisplayName("findById - 命中时返回 Entity")
    void findById_hit() {
        ObjectInstancePO po = buildPO(UUID.randomUUID().toString(), "PK-001");
        Mockito.when(objectInstancePOMapper.selectById(po.getId())).thenReturn(po);

        Optional<ObjectInstance> result = repository.findById(po.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(po.getId());
        assertThat(result.get().getPrimaryKeyValue()).isEqualTo("PK-001");
        assertThat(result.get().getCoreData()).containsEntry("name", "device-PK-001");
        assertThat(result.get().getCoreData()).containsEntry("status", "active");
        assertThat(result.get().getExtendedData()).containsKey("tags");
    }

    @Test
    @DisplayName("findById - 未命中时返回空 Optional")
    void findById_miss() {
        Mockito.when(objectInstancePOMapper.selectById(any())).thenReturn(null);
        assertThat(repository.findById("nope")).isEmpty();
    }

    @Test
    @DisplayName("findByObjectTypeId - 委托 mapper 并分页转换")
    void findByObjectTypeId_paged() {
        List<ObjectInstancePO> poList = List.of(
                buildPO(UUID.randomUUID().toString(), "PK-001"),
                buildPO(UUID.randomUUID().toString(), "PK-002"));
        Mockito.when(objectInstancePOMapper.selectByObjectTypeId(OBJECT_TYPE_ID, 0, 20)).thenReturn(poList);

        List<ObjectInstance> result = repository.findByObjectTypeId(OBJECT_TYPE_ID, 0, 20);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ObjectInstance::getPrimaryKeyValue)
                .containsExactly("PK-001", "PK-002");
        assertThat(result.get(0).getCoreData()).containsEntry("name", "device-PK-001");
    }

    @Test
    @DisplayName("findByObjectTypeId - 空集合")
    void findByObjectTypeId_empty() {
        Mockito.when(objectInstancePOMapper.selectByObjectTypeId(OBJECT_TYPE_ID, 0, 10))
                .thenReturn(Collections.emptyList());
        assertThat(repository.findByObjectTypeId(OBJECT_TYPE_ID, 0, 10)).isEmpty();
    }

    @Test
    @DisplayName("countByObjectTypeId - 直接返回 mapper 结果")
    void countByObjectTypeId() {
        Mockito.when(objectInstancePOMapper.countByObjectTypeId(OBJECT_TYPE_ID)).thenReturn(42L);
        assertThat(repository.countByObjectTypeId(OBJECT_TYPE_ID)).isEqualTo(42L);
    }

    @Test
    @DisplayName("save - 写入 mapper 并填充 createdAt/updatedAt")
    void save_insertsAndStamps() {
        ObjectInstance instance = ObjectInstance.create(
                ONTOLOGY_ID, OBJECT_TYPE_ID, "PK-NEW",
                Map.of("name", "device-new", "status", "draft"));
        instance.setCreatedAt(null);

        ObjectInstance saved = repository.save(instance);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        Mockito.verify(objectInstancePOMapper).insert(any(ObjectInstancePO.class));
    }

    @Test
    @DisplayName("save - 保留已存在的 createdAt")
    void save_preservesCreatedAt() {
        java.time.Instant original = java.time.Instant.parse("2024-01-01T00:00:00Z");
        ObjectInstance instance = ObjectInstance.create(
                ONTOLOGY_ID, OBJECT_TYPE_ID, "PK-EXIST", Map.of("k", "v"));
        instance.setCreatedAt(original);

        ObjectInstance saved = repository.save(instance);

        assertThat(saved.getCreatedAt()).isEqualTo(original);
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("deleteById - 委托 mapper")
    void deleteById() {
        String id = UUID.randomUUID().toString();
        repository.deleteById(id);
        Mockito.verify(objectInstancePOMapper).deleteById(eq(id));
    }

    @Test
    @DisplayName("existsByObjectTypeIdAndPrimaryKeyValue - 命中返回 true")
    void existsByObjectTypeIdAndPrimaryKeyValue_true() {
        ObjectInstancePO po = buildPO(UUID.randomUUID().toString(), "PK-EXISTS");
        Mockito.when(objectInstancePOMapper.selectByObjectTypeIdAndPrimaryKeyValue(OBJECT_TYPE_ID, "PK-EXISTS"))
                .thenReturn(po);

        assertThat(repository.existsByObjectTypeIdAndPrimaryKeyValue(OBJECT_TYPE_ID, "PK-EXISTS")).isTrue();
    }

    @Test
    @DisplayName("existsByObjectTypeIdAndPrimaryKeyValue - 未命中返回 false")
    void existsByObjectTypeIdAndPrimaryKeyValue_false() {
        Mockito.when(objectInstancePOMapper.selectByObjectTypeIdAndPrimaryKeyValue(OBJECT_TYPE_ID, "PK-MISS"))
                .thenReturn(null);

        assertThat(repository.existsByObjectTypeIdAndPrimaryKeyValue(OBJECT_TYPE_ID, "PK-MISS")).isFalse();
    }
}
