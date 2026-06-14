package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.common.enums.upload.FileType;
import com.ontology.platform.common.enums.upload.UploadStatus;
import com.ontology.platform.domain.entity.upload.UploadTask;
import com.ontology.platform.infrastructure.persistence.UploadTaskPO;
import org.springframework.stereotype.Component;

@Component
public class UploadTaskConverter {

    public UploadTask toEntity(UploadTaskPO po) {
        if (po == null) return null;
        UploadTask entity = new UploadTask();
        entity.setId(po.getId());
        entity.setOriginalFileName(po.getOriginalFileName());
        entity.setFileSize(po.getFileSize() != null ? po.getFileSize() : 0);
        entity.setFileType(po.getFileType() != null ? FileType.fromCode(po.getFileType()) : null);
        entity.setChunkSize(po.getChunkSize() != null ? po.getChunkSize() : 0);
        entity.setTotalChunks(po.getTotalChunks() != null ? po.getTotalChunks() : 0);
        entity.setTargetType(po.getTargetType());
        entity.setOntologyId(po.getOntologyId());
        entity.setObjectTypeName(po.getObjectTypeName());
        entity.setUserId(po.getUserId());
        entity.setTenantId(po.getTenantId());
        entity.setStatus(po.getStatus() != null ? UploadStatus.valueOf(po.getStatus()) : null);
        entity.setUploadedChunks(po.getUploadedChunksSet());
        entity.setStoredFilePath(po.getStoredFilePath());
        entity.setFileMd5(po.getFileMd5());
        entity.setExpiresAt(po.getExpiresAt());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }

    public UploadTaskPO toPO(UploadTask entity) {
        if (entity == null) return null;
        UploadTaskPO po = new UploadTaskPO();
        po.setId(entity.getId());
        po.setOriginalFileName(entity.getOriginalFileName());
        po.setFileSize(entity.getFileSize());
        po.setFileType(entity.getFileType() != null ? entity.getFileType().getCode() : null);
        po.setChunkSize(entity.getChunkSize());
        po.setTotalChunks(entity.getTotalChunks());
        po.setTargetType(entity.getTargetType());
        po.setOntologyId(entity.getOntologyId());
        po.setObjectTypeName(entity.getObjectTypeName());
        po.setUserId(entity.getUserId());
        po.setTenantId(entity.getTenantId());
        po.setStatus(entity.getStatus() != null ? entity.getStatus().name() : null);
        po.setUploadedChunksSet(entity.getUploadedChunks());
        po.setStoredFilePath(entity.getStoredFilePath());
        po.setFileMd5(entity.getFileMd5());
        po.setExpiresAt(entity.getExpiresAt());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        return po;
    }
}
