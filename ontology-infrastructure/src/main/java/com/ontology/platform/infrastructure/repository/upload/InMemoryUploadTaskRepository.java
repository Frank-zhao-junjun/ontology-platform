package com.ontology.platform.infrastructure.repository.upload;

import com.ontology.platform.domain.entity.upload.UploadTask;
import com.ontology.platform.domain.repository.upload.UploadTaskRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUploadTaskRepository implements UploadTaskRepository {
    private final Map<String, UploadTask> tasks = new ConcurrentHashMap<>();

    public UploadTask save(UploadTask task) { tasks.put(task.getId(), task); return task; }
    public UploadTask update(UploadTask task) { tasks.put(task.getId(), task); return task; }
    public Optional<UploadTask> findById(String id) { return Optional.ofNullable(tasks.get(id)); }
    public void deleteById(String id) { tasks.remove(id); }
}
