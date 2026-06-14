package com.ontology.platform.infrastructure.repository.upload;

import com.ontology.platform.common.enums.upload.ErrorHandling;
import com.ontology.platform.common.enums.upload.ImportStatus;
import com.ontology.platform.common.enums.upload.MergeStrategy;
import com.ontology.platform.domain.entity.upload.ImportTask;
import com.ontology.platform.infrastructure.converter.ImportTaskConverter;
import com.ontology.platform.infrastructure.persistence.ImportTaskPO;
import com.ontology.platform.infrastructure.persistence.ImportTaskPOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@DisplayName("ImportTaskRepositoryImpl 测试")
class ImportTaskRepositoryImplTest {

    private ImportTaskRepositoryImpl repository;
    private ImportTaskPOMapper mapper;
    private ImportTaskConverter converter;

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(ImportTaskPOMapper.class);
        converter = new ImportTaskConverter();
        repository = new ImportTaskRepositoryImpl(mapper, converter);
    }

    private ImportTaskPO buildPO(String id) {
        return ImportTaskPO.builder()
                .id(id)
                .uploadId("upload-1")
                .ontologyId("ont-1")
                .objectTypeName("Concept")
                .mergeStrategy(MergeStrategy.INSERT.name())
                .errorHandling(ErrorHandling.SKIP.name())
                .userId("user-1")
                .tenantId("tenant-1")
                .status(ImportStatus.PENDING.name())
                .totalRows(100L)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("save - 写入 mapper")
    void save_inserts() {
        ImportTask task = ImportTask.create(
                "upload-1", "ont-1", "Concept",
                MergeStrategy.INSERT, ErrorHandling.SKIP,
                "user-1", "tenant-1");

        ImportTask saved = repository.save(task);

        assertThat(saved.getId()).isEqualTo(task.getId());
        Mockito.verify(mapper).insert(any(ImportTaskPO.class));
    }

    @Test
    @DisplayName("save - 保留现有属性")
    void save_preservesFields() {
        ImportTask task = ImportTask.create(
                "upload-1", "ont-1", "Concept",
                MergeStrategy.INSERT, ErrorHandling.SKIP,
                "user-1", "tenant-1");
        task.setStatus(ImportStatus.PARSING);

        ImportTask saved = repository.save(task);

        assertThat(saved.getStatus()).isEqualTo(ImportStatus.PARSING);
        Mockito.verify(mapper).insert(any(ImportTaskPO.class));
    }

    @Test
    @DisplayName("findById - 命中时返回 Entity")
    void findById_hit() {
        String id = UUID.randomUUID().toString();
        ImportTaskPO po = buildPO(id);
        Mockito.when(mapper.selectById(id)).thenReturn(po);

        Optional<ImportTask> result = repository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getUploadId()).isEqualTo("upload-1");
        assertThat(result.get().getStatus()).isEqualTo(ImportStatus.PENDING);
    }

    @Test
    @DisplayName("findById - 未命中时返回空 Optional")
    void findById_miss() {
        Mockito.when(mapper.selectById(any())).thenReturn(null);

        assertThat(repository.findById("nope")).isEmpty();
    }

    @Test
    @DisplayName("update - 存在时更新")
    void update_existing() {
        String id = UUID.randomUUID().toString();
        ImportTask task = ImportTask.create(
                "upload-1", "ont-1", "Concept",
                MergeStrategy.INSERT, ErrorHandling.SKIP,
                "user-1", "tenant-1");
        task.setId(id);
        task.setStatus(ImportStatus.PARSING);

        ImportTaskPO existing = buildPO(id);
        Mockito.when(mapper.selectById(id)).thenReturn(existing);

        ImportTask updated = repository.update(task);

        assertThat(updated.getStatus()).isEqualTo(ImportStatus.PARSING);
        Mockito.verify(mapper).updateById(any(ImportTaskPO.class));
    }

    @Test
    @DisplayName("update - 不存在时抛 IllegalStateException")
    void update_notFound() {
        String id = UUID.randomUUID().toString();
        ImportTask task = ImportTask.create(
                "upload-1", "ont-1", "Concept",
                MergeStrategy.INSERT, ErrorHandling.SKIP,
                "user-1", "tenant-1");
        task.setId(id);

        Mockito.when(mapper.selectById(id)).thenReturn(null);

        assertThatThrownBy(() -> repository.update(task))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ImportTask not found");
    }

    @Test
    @DisplayName("findById - mapper 返回 null 时 converter 返回 null")
    void findById_nullPONullEntity() {
        Mockito.when(mapper.selectById(any())).thenReturn(null);

        Optional<ImportTask> result = repository.findById("nonexistent");

        assertThat(result).isEmpty();
        Mockito.verify(mapper).selectById(eq("nonexistent"));
    }
}
