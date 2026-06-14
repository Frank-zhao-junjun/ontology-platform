package com.ontology.platform.domain.entity.upload;

import com.ontology.platform.common.enums.upload.FileType;
import com.ontology.platform.common.enums.upload.UploadStatus;
import lombok.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadTask {
    private String id;
    private String originalFileName;
    private long fileSize;
    private FileType fileType;
    private int chunkSize;
    private int totalChunks;
    private String targetType;
    private String ontologyId;
    private String objectTypeName;
    private String userId;
    private String tenantId;
    private UploadStatus status;
    @Builder.Default
    private Set<Integer> uploadedChunks = new HashSet<>();
    private String storedFilePath;
    private String fileMd5;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static UploadTask create(String fileName, long fileSize, FileType fileType, int chunkSize,
                                    String targetType, String ontologyId, String objectTypeName,
                                    String userId, String tenantId) {
        int totalChunks = (int) Math.ceil((double) fileSize / chunkSize);
        return UploadTask.builder()
                .id("upload_" + UUID.randomUUID().toString())
                .originalFileName(fileName)
                .fileSize(fileSize)
                .fileType(fileType)
                .chunkSize(chunkSize)
                .totalChunks(Math.max(totalChunks, 1))
                .targetType(targetType)
                .ontologyId(ontologyId)
                .objectTypeName(objectTypeName)
                .userId(userId)
                .tenantId(tenantId)
                .status(UploadStatus.PENDING)
                .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public void markExpired() {
        status = UploadStatus.EXPIRED;
        updatedAt = Instant.now();
    }

    public boolean isChunkUploaded(int chunkNumber) {
        return uploadedChunks.contains(chunkNumber);
    }

    public void markChunkUploaded(int chunkNumber) {
        if (chunkNumber < 1 || chunkNumber > totalChunks) {
            throw new IllegalArgumentException(
                    "Invalid chunk number: " + chunkNumber + ". Must be between 1 and " + totalChunks);
        }
        uploadedChunks.add(chunkNumber);
        status = isAllChunksUploaded() ? UploadStatus.COMPLETED : UploadStatus.UPLOADING;
        updatedAt = Instant.now();
    }

    public boolean isAllChunksUploaded() {
        return uploadedChunks.size() >= totalChunks;
    }

    public Set<Integer> getMissingChunks() {
        return IntStream.rangeClosed(1, totalChunks)
                .filter(i -> !uploadedChunks.contains(i))
                .boxed()
                .collect(Collectors.toSet());
    }

    public int getProgressPercent() {
        return totalChunks == 0 ? 0 : (int) Math.round(uploadedChunks.size() * 100.0 / totalChunks);
    }

    public void markCompleted(String storedFilePath, String fileMd5) {
        this.storedFilePath = storedFilePath;
        this.fileMd5 = fileMd5;
        this.status = UploadStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public String getFileName() {
        return originalFileName;
    }
}
