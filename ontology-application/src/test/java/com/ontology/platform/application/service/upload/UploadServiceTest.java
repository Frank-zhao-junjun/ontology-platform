package com.ontology.platform.application.service.upload;

import com.ontology.platform.application.dto.upload.InitUploadRequest;
import com.ontology.platform.application.dto.upload.UploadTaskResponse;
import com.ontology.platform.application.service.impl.upload.UploadServiceImpl;
import com.ontology.platform.common.enums.upload.FileType;
import com.ontology.platform.common.exception.upload.UploadException;
import com.ontology.platform.domain.entity.upload.UploadTask;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.upload.UploadTaskRepository;
import com.ontology.platform.domain.service.upload.DataExportService;
import com.ontology.platform.domain.service.upload.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 上传服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("上传服务测试")
class UploadServiceTest {

    @Mock
    private UploadTaskRepository uploadTaskRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private DataExportService dataExportService;

    @Mock
    private ObjectTypeRepository objectTypeRepository;

    private UploadService uploadService;

    @BeforeEach
    void setUp() {
        uploadService = new UploadServiceImpl(
                uploadTaskRepository,
                fileStorageService,
                dataExportService,
                objectTypeRepository
        );
    }

    @Test
    @DisplayName("初始化上传任务 - 成功")
    void initUpload_Success() {
        // given
        InitUploadRequest request = InitUploadRequest.builder()
                .fileName("test.csv")
                .fileSize(10485760L) // 10MB
                .fileType("csv")
                .targetType("object_import")
                .ontologyId("ontology-001")
                .objectTypeName("customer")
                .build();

        when(fileStorageService.initChunkDirectory(anyString())).thenReturn("/tmp/uploads/test");
        when(uploadTaskRepository.save(any(UploadTask.class))).thenAnswer(i -> i.getArgument(0));

        // when
        UploadTaskResponse response = uploadService.initUpload(request, "user-001", "tenant-001");

        // then
        assertNotNull(response);
        assertNotNull(response.getUploadId());
        assertEquals("test.csv", response.getFileName());
        assertEquals(10485760L, response.getFileSize());
        assertEquals("pending", response.getStatus());
        assertNotNull(response.getUploadUrls());
        assertTrue(response.getTotalChunks() > 0);

        verify(fileStorageService).initChunkDirectory(anyString());
        verify(uploadTaskRepository).save(any(UploadTask.class));
    }

    @Test
    @DisplayName("初始化上传任务 - 文件过大")
    void initUpload_FileTooLarge() {
        // given
        InitUploadRequest request = InitUploadRequest.builder()
                .fileName("large.csv")
                .fileSize(200000000L) // 200MB
                .fileType("csv")
                .targetType("object_import")
                .build();

        // when & then
        assertThrows(UploadException.class, () ->
                uploadService.initUpload(request, "user-001", "tenant-001"));
    }

    @Test
    @DisplayName("初始化上传任务 - 不支持的文件类型")
    void initUpload_UnsupportedFileType() {
        // given
        InitUploadRequest request = InitUploadRequest.builder()
                .fileName("test.pdf")
                .fileSize(1024000L)
                .fileType("pdf")
                .targetType("object_import")
                .build();

        // when & then
        assertThrows(UploadException.class, () ->
                uploadService.initUpload(request, "user-001", "tenant-001"));
    }

    @Test
    @DisplayName("上传分片 - 成功")
    void uploadChunk_Success() {
        // given
        String uploadId = "upload_test123";
        UploadTask task = UploadTask.builder()
                .id(uploadId)
                .fileName("test.csv")
                .fileSize(10485760L)
                .fileType(FileType.CSV)
                .chunkSize(5242880)
                .totalChunks(2)
                .uploadedChunks(new java.util.HashSet<>())
                .build();

        when(uploadTaskRepository.findById(uploadId)).thenReturn(Optional.of(task));
        when(uploadTaskRepository.update(any(UploadTask.class))).thenAnswer(i -> i.getArgument(0));

        // when
        UploadTaskResponse response = uploadService.uploadChunk(
                uploadId, 1, new byte[1024], null);

        // then
        assertNotNull(response);
        assertEquals(uploadId, response.getUploadId());
        assertEquals(50, response.getProgressPercent());

        verify(fileStorageService).saveChunk(eq(uploadId), eq(1), any(byte[].class));
        verify(uploadTaskRepository).update(any(UploadTask.class));
    }

    @Test
    @DisplayName("上传分片 - 任务不存在")
    void uploadChunk_TaskNotFound() {
        // given
        String uploadId = "non-existent";
        when(uploadTaskRepository.findById(uploadId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(UploadException.class, () ->
                uploadService.uploadChunk(uploadId, 1, new byte[1024], null));
    }

    @Test
    @DisplayName("上传分片 - 分片编号超出范围")
    void uploadChunk_ChunkOutOfRange() {
        // given
        String uploadId = "upload_test123";
        UploadTask task = UploadTask.builder()
                .id(uploadId)
                .fileName("test.csv")
                .fileSize(5242880L)
                .fileType(FileType.CSV)
                .chunkSize(5242880)
                .totalChunks(1)
                .uploadedChunks(new java.util.HashSet<>())
                .build();

        when(uploadTaskRepository.findById(uploadId)).thenReturn(Optional.of(task));

        // when & then
        assertThrows(UploadException.class, () ->
                uploadService.uploadChunk(uploadId, 5, new byte[1024], null));
    }

    @Test
    @DisplayName("查询上传状态 - 成功")
    void getUploadStatus_Success() {
        // given
        String uploadId = "upload_test123";
        UploadTask task = UploadTask.builder()
                .id(uploadId)
                .fileName("test.csv")
                .fileSize(10485760L)
                .fileType(FileType.CSV)
                .chunkSize(5242880)
                .totalChunks(2)
                .status(com.ontology.platform.common.enums.upload.UploadStatus.UPLOADING)
                .uploadedChunks(new java.util.HashSet<>())
                .build();
        task.getUploadedChunks().add(1);

        when(uploadTaskRepository.findById(uploadId)).thenReturn(Optional.of(task));

        // when
        UploadTaskResponse response = uploadService.getUploadStatus(uploadId);

        // then
        assertNotNull(response);
        assertEquals(uploadId, response.getUploadId());
        assertEquals("uploading", response.getStatus());
        assertEquals(50, response.getProgressPercent());
        assertEquals(1, response.getUploadedChunks().size());
    }

    @Test
    @DisplayName("取消上传 - 成功")
    void cancelUpload_Success() {
        // given
        String uploadId = "upload_test123";
        UploadTask task = UploadTask.builder()
                .id(uploadId)
                .fileName("test.csv")
                .uploadedChunks(new java.util.HashSet<>())
                .build();

        when(uploadTaskRepository.findById(uploadId)).thenReturn(Optional.of(task));
        when(fileStorageService.deleteChunks(uploadId)).thenReturn(2);

        // when
        int deletedChunks = uploadService.cancelUpload(uploadId);

        // then
        assertEquals(2, deletedChunks);
        verify(fileStorageService).deleteChunks(uploadId);
        verify(uploadTaskRepository).deleteById(uploadId);
    }
}
