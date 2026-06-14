package com.ontology.platform.infrastructure.repository.upload;

import com.ontology.platform.domain.entity.upload.UploadTask;
import com.ontology.platform.domain.repository.upload.UploadTaskRepository;
import com.ontology.platform.infrastructure.converter.UploadTaskConverter;
import com.ontology.platform.infrastructure.persistence.UploadTaskPO;
import com.ontology.platform.infrastructure.persistence.UploadTaskPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UploadTaskRepositoryImpl implements UploadTaskRepository {

    private final UploadTaskPOMapper uploadTaskPOMapper;
    private final UploadTaskConverter uploadTaskConverter;

    @Override
    public UploadTask save(UploadTask task) {
        log.debug("Saving upload task: {}", task.getId());
        if (task.getCreatedAt() == null) {
            task.setCreatedAt(Instant.now());
        }
        task.setUpdatedAt(Instant.now());
        UploadTaskPO po = uploadTaskConverter.toPO(task);
        uploadTaskPOMapper.insert(po);
        return task;
    }

    @Override
    public UploadTask update(UploadTask task) {
        log.debug("Updating upload task: {}", task.getId());
        if (uploadTaskPOMapper.selectById(task.getId()) == null) {
            throw new IllegalStateException("UploadTask not found: " + task.getId());
        }
        task.setUpdatedAt(Instant.now());
        UploadTaskPO po = uploadTaskConverter.toPO(task);
        uploadTaskPOMapper.updateById(po);
        return task;
    }

    @Override
    public Optional<UploadTask> findById(String id) {
        log.debug("Finding upload task by id: {}", id);
        UploadTaskPO po = uploadTaskPOMapper.selectById(id);
        return Optional.ofNullable(uploadTaskConverter.toEntity(po));
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting upload task by id: {}", id);
        uploadTaskPOMapper.deleteById(id);
    }
}
