package com.ontology.platform.domain.entity.upload;

import com.ontology.platform.common.enums.upload.FileType;
import com.ontology.platform.common.enums.upload.UploadStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 上传任务实体单元测试
 */
@DisplayName("上传任务实体测试")
class UploadTaskTest {

    @Test
    @DisplayName("创建上传任务 - 成功")
    void create_Success() {
        // when
        UploadTask task = UploadTask.create(
                "test.csv",
                10485760L,
                FileType.CSV,
                5242880,
                "object_import",
                "ontology-001",
                "customer",
                "user-001",
                "tenant-001"
        );

        // then
        assertNotNull(task);
        assertNotNull(task.getId());
        assertTrue(task.getId().startsWith("upload_"));
        assertEquals("test.csv", task.getOriginalFileName());
        assertEquals(10485760L, task.getFileSize());
        assertEquals(FileType.CSV, task.getFileType());
        assertEquals(5242880, task.getChunkSize());
        assertEquals(2, task.getTotalChunks()); // 10MB / 5MB = 2 chunks
        assertEquals(UploadStatus.PENDING, task.getStatus());
        assertEquals("object_import", task.getTargetType());
        assertEquals("ontology-001", task.getOntologyId());
        assertEquals("customer", task.getObjectTypeName());
        assertEquals("user-001", task.getUserId());
        assertEquals("tenant-001", task.getTenantId());
        assertNotNull(task.getCreatedAt());
        assertNotNull(task.getExpiresAt());
        assertNotNull(task.getUploadedChunks());
        assertTrue(task.getUploadedChunks().isEmpty());
    }

    @Test
    @DisplayName("标记分片已上传 - 成功")
    void markChunkUploaded_Success() {
        // given
        UploadTask task = UploadTask.create(
                "test.csv",
                10485760L,
                FileType.CSV,
                5242880,
                "object_import",
                "ontology-001",
                "customer",
                "user-001",
                "tenant-001"
        );

        // when
        task.markChunkUploaded(1);

        // then
        assertEquals(UploadStatus.UPLOADING, task.getStatus());
        assertEquals(1, task.getUploadedChunks().size());
        assertTrue(task.getUploadedChunks().contains(1));
        assertEquals(50, task.getProgressPercent());
    }

    @Test
    @DisplayName("标记分片已上传 - 无效分片编号")
    void markChunkUploaded_InvalidChunkNumber() {
        // given
        UploadTask task = UploadTask.create(
                "test.csv",
                10485760L,
                FileType.CSV,
                5242880,
                "object_import",
                "ontology-001",
                "customer",
                "user-001",
                "tenant-001"
        );

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                task.markChunkUploaded(0));
        assertThrows(IllegalArgumentException.class, () ->
                task.markChunkUploaded(3));
    }

    @Test
    @DisplayName("所有分片上传完成 - 状态变更")
    void markChunkUploaded_AllChunksUploaded() {
        // given
        UploadTask task = UploadTask.create(
                "test.csv",
                10485760L,
                FileType.CSV,
                5242880,
                "object_import",
                "ontology-001",
                "customer",
                "user-001",
                "tenant-001"
        );

        // when
        task.markChunkUploaded(1);
        task.markChunkUploaded(2);

        // then
        assertEquals(UploadStatus.VERIFYING, task.getStatus());
        assertEquals(2, task.getUploadedChunks().size());
        assertEquals(100, task.getProgressPercent());
        assertTrue(task.isAllChunksUploaded());
    }

    @Test
    @DisplayName("获取缺失的分片列表")
    void getMissingChunks() {
        // given
        UploadTask task = UploadTask.create(
                "test.csv",
                15728640L, // 15MB
                FileType.CSV,
                5242880, // 5MB/chunk = 3 chunks
                "object_import",
                "ontology-001",
                "customer",
                "user-001",
                "tenant-001"
        );
        task.markChunkUploaded(1);

        // when
        var missingChunks = task.getMissingChunks();

        // then
        assertEquals(2, missingChunks.size());
        assertTrue(missingChunks.contains(2));
        assertTrue(missingChunks.contains(3));
    }

    @Test
    @DisplayName("检查分片是否已上传")
    void isChunkUploaded() {
        // given
        UploadTask task = UploadTask.create(
                "test.csv",
                10485760L,
                FileType.CSV,
                5242880,
                "object_import",
                "ontology-001",
                "customer",
                "user-001",
                "tenant-001"
        );
        task.markChunkUploaded(1);

        // then
        assertTrue(task.isChunkUploaded(1));
        assertFalse(task.isChunkUploaded(2));
    }

    @Test
    @DisplayName("标记上传完成")
    void markCompleted() {
        // given
        UploadTask task = UploadTask.create(
                "test.csv",
                10485760L,
                FileType.CSV,
                5242880,
                "object_import",
                "ontology-001",
                "customer",
                "user-001",
                "tenant-001"
        );

        // when
        task.markCompleted("/data/uploads/test.csv", "abc123");

        // then
        assertEquals(UploadStatus.PROCESSING, task.getStatus());
        assertEquals("/data/uploads/test.csv", task.getStoredFilePath());
        assertEquals("abc123", task.getFileMd5());
    }

    @Test
    @DisplayName("标记上传失败")
    void markFailed() {
        // given
        UploadTask task = UploadTask.create(
                "test.csv",
                10485760L,
                FileType.CSV,
                5242880,
                "object_import",
                "ontology-001",
                "customer",
                "user-001",
                "tenant-001"
        );

        // when
        task.markFailed();

        // then
        assertEquals(UploadStatus.FAILED, task.getStatus());
    }
}
