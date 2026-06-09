package com.ontology.platform.infrastructure.repository.upload;

import com.ontology.platform.domain.entity.upload.ImportTask;
import com.ontology.platform.domain.repository.upload.ImportTaskRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryImportTaskRepository implements ImportTaskRepository {
    private final Map<String, ImportTask> tasks = new ConcurrentHashMap<>();

    public ImportTask save(ImportTask task) { tasks.put(task.getId(), task); return task; }
    public ImportTask update(ImportTask task) { tasks.put(task.getId(), task); return task; }
    public Optional<ImportTask> findById(String id) { return Optional.ofNullable(tasks.get(id)); }
}
