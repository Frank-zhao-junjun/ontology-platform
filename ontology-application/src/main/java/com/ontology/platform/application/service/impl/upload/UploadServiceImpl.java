package com.ontology.platform.application.service.impl.upload;

import com.ontology.platform.application.dto.upload.InitUploadRequest;
import com.ontology.platform.application.dto.upload.UploadTaskResponse;
import com.ontology.platform.application.service.upload.UploadService;
import com.ontology.platform.common.enums.upload.FileType;
import com.ontology.platform.common.exception.upload.UploadException;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.upload.UploadTask;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.upload.UploadTaskRepository;
import com.ontology.platform.domain.service.upload.DataExportService;
import com.ontology.platform.domain.service.upload.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UploadServiceImpl implements UploadService {

    private final UploadTaskRepository uploadTaskRepository;
    private final FileStorageService fileStorageService;
    private final DataExportService dataExportService;
    private final ObjectTypeRepository objectTypeRepository;

    private static final int DEFAULT_CHUNK_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${upload.max-file-size:104857600}")
    private long maxFileSize;

    @Override
    @Transactional
    public UploadTaskResponse initUpload(InitUploadRequest request, String userId, String tenantId) {
        log.info("Initializing upload: fileName={}, fileSize={}, userId={}", 
                request.getFileName(), request.getFileSize(), userId);

        if (request.getFileSize() > maxFileSize) {
            throw UploadException.fileTooLarge(maxFileSize);
        }

        FileType fileType = FileType.fromCode(request.getFileType());
        if (fileType == null) {
            throw UploadException.unsupportedFileType(request.getFileType());
        }

        int chunkSize = request.getChunkSize() != null ? request.getChunkSize() : DEFAULT_CHUNK_SIZE;

        UploadTask task = UploadTask.create(
                request.getFileName(),
                request.getFileSize(),
                fileType,
                chunkSize,
                request.getTargetType(),
                request.getOntologyId(),
                request.getObjectTypeName(),
                userId,
                tenantId
        );

        fileStorageService.initChunkDirectory(task.getId());
        uploadTaskRepository.save(task);

        Map<String, String> uploadUrls = new HashMap<>();
        for (int i = 1; i <= task.getTotalChunks(); i++) {
            uploadUrls.put(String.valueOf(i), "/api/v1/uploads/" + task.getId() + "/chunks/" + i);
        }

        log.info("Upload task created: uploadId={}, totalChunks={}", task.getId(), task.getTotalChunks());

        return toUploadTaskResponse(task, uploadUrls);
    }

    @Override
    @Transactional
    public UploadTaskResponse uploadChunk(String uploadId, int chunkNumber, 
            byte[] chunkData, String contentMd5) {
        log.debug("Uploading chunk: uploadId={}, chunkNumber={}, size={}", 
                uploadId, chunkNumber, chunkData.length);

        UploadTask task = uploadTaskRepository.findById(uploadId)
                .orElseThrow(() -> UploadException.uploadNotFound(uploadId));

        if (task.isExpired()) {
            task.markExpired();
            uploadTaskRepository.update(task);
            throw UploadException.uploadExpired(uploadId);
        }

        if (chunkNumber < 1 || chunkNumber > task.getTotalChunks()) {
            throw UploadException.chunkOutOfRange(chunkNumber, task.getTotalChunks());
        }

        if (task.isChunkUploaded(chunkNumber)) {
            log.info("Chunk already uploaded: uploadId={}, chunkNumber={}", uploadId, chunkNumber);
            return toUploadTaskResponse(task, null);
        }

        fileStorageService.saveChunk(uploadId, chunkNumber, chunkData);
        task.markChunkUploaded(chunkNumber);
        uploadTaskRepository.update(task);

        log.debug("Chunk uploaded successfully: uploadId={}, chunkNumber={}", uploadId, chunkNumber);

        return toUploadTaskResponse(task, null);
    }

    @Override
    public UploadTaskResponse getUploadStatus(String uploadId) {
        UploadTask task = uploadTaskRepository.findById(uploadId)
                .orElseThrow(() -> UploadException.uploadNotFound(uploadId));
        return toUploadTaskResponse(task, null);
    }

    @Override
    @Transactional
    public UploadTaskResponse completeUpload(String uploadId, String finalMd5) {
        log.info("Completing upload: uploadId={}", uploadId);

        UploadTask task = uploadTaskRepository.findById(uploadId)
                .orElseThrow(() -> UploadException.uploadNotFound(uploadId));

        if (!task.isAllChunksUploaded()) {
            throw new UploadException(
                    com.ontology.platform.common.enums.ErrorCode.VALIDATION_ERROR,
                    "Not all chunks have been uploaded. Missing: " + task.getMissingChunks()
            );
        }

        String storedFilePath = fileStorageService.mergeChunks(
                uploadId, task.getTotalChunks(), task.getFileName()
        );

        String fileMd5 = fileStorageService.calculateMd5(storedFilePath);

        if (finalMd5 != null && !finalMd5.equalsIgnoreCase(fileMd5)) {
            log.error("File MD5 mismatch: expected={}, actual={}", finalMd5, fileMd5);
            throw UploadException.fileVerificationFailed();
        }

        task.markCompleted(storedFilePath, fileMd5);
        uploadTaskRepository.update(task);

        log.info("Upload completed: uploadId={}, fileMd5={}", uploadId, fileMd5);

        UploadTaskResponse response = toUploadTaskResponse(task, null);
        response.setFileMd5(fileMd5);
        response.setFileId(task.getId());
        response.setFileUrl("/api/v1/files/" + task.getId());
        response.setMessage("文件已接收，正在后台处理");
        
        return response;
    }

    @Override
    @Transactional
    public int cancelUpload(String uploadId) {
        log.info("Cancelling upload: uploadId={}", uploadId);

        UploadTask task = uploadTaskRepository.findById(uploadId)
                .orElseThrow(() -> UploadException.uploadNotFound(uploadId));

        int deletedChunks = fileStorageService.deleteChunks(uploadId);
        uploadTaskRepository.deleteById(uploadId);

        log.info("Upload cancelled: uploadId={}, deletedChunks={}", uploadId, deletedChunks);

        return deletedChunks;
    }

    @Override
    public byte[] getImportTemplate(String ontologyId, String objectTypeName, String format) {
        log.info("Getting import template: ontologyId={}, objectType={}, format={}", 
                ontologyId, objectTypeName, format);

        ObjectType objectType = objectTypeRepository.findById(objectTypeName)
                .orElseThrow(() -> new com.ontology.platform.common.exception.ResourceNotFoundException(
                        "ObjectType", objectTypeName));

        var properties = objectType.getProperties().stream()
                .map(p -> p.getName())
                .toList();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        if ("xlsx".equalsIgnoreCase(format)) {
            dataExportService.generateImportTemplateExcel(
                    objectTypeName, properties, outputStream, objectTypeName + "_template"
            );
        } else {
            dataExportService.generateImportTemplateCsv(objectTypeName, properties, outputStream);
        }

        return outputStream.toByteArray();
    }

    private UploadTaskResponse toUploadTaskResponse(UploadTask task, Map<String, String> uploadUrls) {
        return UploadTaskResponse.builder()
                .uploadId(task.getId())
                .fileName(task.getOriginalFileName())
                .fileSize(task.getFileSize())
                .chunkSize(task.getChunkSize())
                .chunkCount(task.getTotalChunks())
                .status(task.getStatus().getCode())
                .uploadedChunks(task.getUploadedChunks())
                .missingChunks(task.getMissingChunks())
                .progressPercent(task.getProgressPercent())
                .uploadUrls(uploadUrls)
                .expiresAt(task.getExpiresAt())
                .createdAt(task.getCreatedAt())
                .build();
    }
}
