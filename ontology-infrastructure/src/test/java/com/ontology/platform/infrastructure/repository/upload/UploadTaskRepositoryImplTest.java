package com.ontology.platform.infrastructure.repository.upload;

import com.ontology.platform.common.enums.upload.FileType;
import com.ontology.platform.common.enums.upload.UploadStatus;
import com.ontology.platform.domain.entity.upload.UploadTask;
import com.ontology.platform.infrastructure.converter.UploadTaskConverter;
import com.ontology.platform.infrastructure.persistence.UploadTaskPO;
import com.ontology.platform.infrastructure.persistence.UploadTaskPOMapper;
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

@DisplayName("UploadTaskRepositoryImpl 测试")
class UploadTaskRepositoryImplTest {

    private UploadTaskRepositoryImpl repository;
    private UploadTaskPOMapper mapper;
    private UploadTaskConverter converter;

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(UploadTaskPOMapper.class);
        converter = new UploadTaskConverter();
        repository = new UploadTaskRepositoryImpl(mapper, converter);
    }

    private UploadTaskPO buildPO(String id) {
        return UploadTaskPO.builder()
                .id(id)
                .originalFileName("test.csv")
                .fileSize(1024L)
                .fileType("CSV")
                .chunkSize(256)
                .totalChunks(4)
                .status(UploadStatus.PENDING.name())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("save - 写入 mapper 并填充 createdAt/updatedAt")
    void save_insertsAndStamps() {
        UploadTask task = UploadTask.create(
                "test.csv", 1024, FileType.CSV, 256,
                "import", "ont-1", "Concept", "user-1", "tenant-1");
        task.setCreatedAt(null);

        UploadTask saved = repository.save(task);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        Mockito.verify(mapper).insert(any(UploadTaskPO.class));
    }

    @Test
    @DisplayName("save - 已有 createdAt 时不覆盖")
    void save_preservesCreatedAt() {
        Instant fixed = Instant.now().minusSeconds(3600);
        UploadTask task = UploadTask.create(
                "test.csv", 1024, FileType.CSV, 256,
                "import", "ont-1", "Concept", "user-1", "tenant-1");
        task.setCreatedAt(fixed);

        UploadTask saved = repository.save(task);

        assertThat(saved.getCreatedAt()).isEqualTo(fixed);
        assertThat(saved.getUpdatedAt()).isNotNull();
        Mockito.verify(mapper).insert(any(UploadTaskPO.class));
    }

    @Test
    @DisplayName("findById - 命中时返回 Entity")
    void findById_hit() {
        String id = UUID.randomUUID().toString();
        UploadTaskPO po = buildPO(id);
        Mockito.when(mapper.selectById(id)).thenReturn(po);

        Optional<UploadTask> result = repository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getOriginalFileName()).isEqualTo("test.csv");
        assertThat(result.get().getStatus()).isEqualTo(UploadStatus.PENDING);
    }

    @Test
    @DisplayName("findById - 未命中时返回空 Optional")
    void findById_miss() {
        Mockito.when(mapper.selectById(any())).thenReturn(null);

        assertThat(repository.findById("nope")).isEmpty();
    }

    @Test
    @DisplayName("update - 存在时更新并刷新 updatedAt")
    void update_existing() {
        String id = UUID.randomUUID().toString();
        UploadTask task = UploadTask.create(
                "test.csv", 1024, FileType.CSV, 256,
                "import", "ont-1", "Concept", "user-1", "tenant-1");
        task.setId(id);
        task.setCreatedAt(Instant.now().minusSeconds(3600));

        UploadTaskPO existing = buildPO(id);
        Mockito.when(mapper.selectById(id)).thenReturn(existing);

        UploadTask updated = repository.update(task);

        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedAt()).isAfter(task.getCreatedAt());
        Mockito.verify(mapper).updateById(any(UploadTaskPO.class));
    }

    @Test
    @DisplayName("update - 不存在时抛 IllegalStateException")
    void update_notFound() {
        String id = UUID.randomUUID().toString();
        UploadTask task = UploadTask.create(
                "test.csv", 1024, FileType.CSV, 256,
                "import", "ont-1", "Concept", "user-1", "tenant-1");
        task.setId(id);

        Mockito.when(mapper.selectById(id)).thenReturn(null);

        assertThatThrownBy(() -> repository.update(task))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UploadTask not found");
    }

    @Test
    @DisplayName("deleteById - 委托 mapper")
    void deleteById() {
        String id = UUID.randomUUID().toString();

        repository.deleteById(id);

        Mockito.verify(mapper).deleteById(eq(id));
    }
}
