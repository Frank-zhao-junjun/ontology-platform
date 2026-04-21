package com.ontology.platform.application.service.upload;

import com.ontology.platform.application.dto.upload.ImportRequest;
import com.ontology.platform.application.dto.upload.ImportTaskResponse;
import com.ontology.platform.application.service.impl.upload.ImportServiceImpl;
import com.ontology.platform.common.enums.upload.ImportStatus;
import com.ontology.platform.common.enums.upload.MergeStrategy;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.upload.UploadTask;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.upload.ImportTaskRepository;
import com.ontology.platform.domain.repository.upload.UploadTaskRepository;
import com.ontology.platform.domain.service.upload.DataFileParser;
import com.ontology.platform.domain.service.upload.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 导入服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("导入服务测试")
class ImportServiceTest {

    @Mock
    private ImportTaskRepository importTaskRepository;

    @Mock
    private UploadTaskRepository uploadTaskRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private DataFileParser dataFileParser;

    @Mock
    private ObjectTypeRepository objectTypeRepository;

    private ImportService importService;

    @BeforeEach
    void setUp() {
        importService = new ImportServiceImpl(
                importTaskRepository,
                uploadTaskRepository,
                fileStorageService,
                dataFileParser,
                objectTypeRepository
        );
    }

    @Test
    @DisplayName("获取导入状态 - 成功")
    void getImportStatus_Success() {
        // given
        String importId = "import_test123";
        com.ontology.platform.domain.entity.upload.ImportTask task = 
                com.ontology.platform.domain.entity.upload.ImportTask.builder()
                .id(importId)
                .uploadId("upload_test")
                .ontologyId("ontology-001")
                .objectTypeName("customer")
                .status(ImportStatus.IMPORTING)
                .totalRows(1000)
                .processedRows(500)
                .successRows(495)
                .failedRows(5)
                .createdAt(Instant.now())
                .build();
        task.setErrors(List.of(
                com.ontology.platform.domain.entity.upload.ImportTask.ImportError.builder()
                        .row(10)
                        .field("name")
                        .message("Name cannot be empty")
                        .build()
        ));

        when(importTaskRepository.findById(importId)).thenReturn(Optional.of(task));

        // when
        ImportTaskResponse response = importService.getImportStatus(importId);

        // then
        assertNotNull(response);
        assertEquals(importId, response.getImportId());
        assertEquals("importing", response.getStatus());
        assertEquals(1000, response.getProgress().getTotalRows());
        assertEquals(500, response.getProgress().getProcessedRows());
        assertEquals(50, response.getProgress().getProgressPercent());
        assertEquals(1, response.getErrors().size());
        assertEquals("name", response.getErrors().get(0).getField());
    }

    @Test
    @DisplayName("获取导入状态 - 任务不存在")
    void getImportStatus_NotFound() {
        // given
        String importId = "non-existent";
        when(importTaskRepository.findById(importId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(
                com.ontology.platform.common.exception.ResourceNotFoundException.class,
                () -> importService.getImportStatus(importId)
        );
    }

    @Test
    @DisplayName("取消导入 - 成功")
    void cancelImport_Success() {
        // given
        String importId = "import_test123";
        com.ontology.platform.domain.entity.upload.ImportTask task = 
                com.ontology.platform.domain.entity.upload.ImportTask.builder()
                .id(importId)
                .status(ImportStatus.IMPORTING)
                .build();

        when(importTaskRepository.findById(importId)).thenReturn(Optional.of(task));
        when(importTaskRepository.update(any())).thenAnswer(i -> i.getArgument(0));

        // when
        importService.cancelImport(importId);

        // then
        verify(importTaskRepository).update(argThat(t -> 
                t.getStatus() == ImportStatus.CANCELLED));
    }

    @Test
    @DisplayName("批量验证数据 - 成功")
    void batchValidate_Success() {
        // given
        ObjectType objectType = ObjectType.builder()
                .id("type_customer")
                .name("customer")
                .properties(new ArrayList<>())
                .build();
        
        var property = com.ontology.platform.domain.vo.Property.builder()
                .id("prop_001")
                .name("customer_name")
                .dataType(com.ontology.platform.common.enums.PropertyDataType.STRING)
                .isRequired(true)
                .build();
        objectType.getProperties().add(property);

        when(objectTypeRepository.findById("type_customer"))
                .thenReturn(Optional.of(objectType));

        List<Map<String, String>> dataList = List.of(
                Map.of("customer_name", "John"),
                Map.of("customer_name", ""),  // 必填字段为空
                Map.of("customer_name", "Jane")
        );

        // when
        ImportService.ValidationResult result = 
                importService.batchValidate(dataList, "type_customer");

        // then
        assertFalse(result.valid());
        assertEquals(3, result.totalRows());
        assertEquals(2, result.successRows());
        assertEquals(1, result.failedRows());
        assertEquals(1, result.errors().size());
        assertEquals(2, result.errors().get(0).row());
        assertEquals("customer_name", result.errors().get(0).field());
    }

    @Test
    @DisplayName("批量验证数据 - 空数据")
    void batchValidate_EmptyData() {
        // given
        ObjectType objectType = ObjectType.builder()
                .id("type_customer")
                .name("customer")
                .properties(new ArrayList<>())
                .build();

        when(objectTypeRepository.findById("type_customer"))
                .thenReturn(Optional.of(objectType));

        List<Map<String, String>> dataList = Collections.emptyList();

        // when
        ImportService.ValidationResult result = 
                importService.batchValidate(dataList, "type_customer");

        // then
        assertTrue(result.valid());
        assertEquals(0, result.totalRows());
        assertEquals(0, result.successRows());
        assertEquals(0, result.failedRows());
    }
}
