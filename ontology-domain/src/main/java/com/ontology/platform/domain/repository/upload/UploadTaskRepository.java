package com.ontology.platform.domain.repository.upload;

import com.ontology.platform.domain.entity.upload.UploadTask;

import java.util.Optional;

public interface UploadTaskRepository {
    UploadTask save(UploadTask task);
    UploadTask update(UploadTask task);
    Optional<UploadTask> findById(String id);
    void deleteById(String id);
}
