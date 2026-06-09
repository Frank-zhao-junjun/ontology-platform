package com.ontology.platform.domain.repository.upload;

import com.ontology.platform.domain.entity.upload.ImportTask;

import java.util.Optional;

public interface ImportTaskRepository {
    ImportTask save(ImportTask task);
    ImportTask update(ImportTask task);
    Optional<ImportTask> findById(String id);
}
