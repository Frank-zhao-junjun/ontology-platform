package com.ontology.platform.infrastructure.repository.upload;

import com.ontology.platform.domain.entity.upload.ImportTask;
import com.ontology.platform.domain.repository.upload.ImportTaskRepository;
import com.ontology.platform.infrastructure.converter.ImportTaskConverter;
import com.ontology.platform.infrastructure.persistence.ImportTaskPO;
import com.ontology.platform.infrastructure.persistence.ImportTaskPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ImportTaskRepositoryImpl implements ImportTaskRepository {

    private final ImportTaskPOMapper importTaskPOMapper;
    private final ImportTaskConverter importTaskConverter;

    @Override
    public ImportTask save(ImportTask task) {
        log.debug("Saving import task: {}", task.getId());
        ImportTaskPO po = importTaskConverter.toPO(task);
        importTaskPOMapper.insert(po);
        return task;
    }

    @Override
    public ImportTask update(ImportTask task) {
        log.debug("Updating import task: {}", task.getId());
        if (importTaskPOMapper.selectById(task.getId()) == null) {
            throw new IllegalStateException("ImportTask not found: " + task.getId());
        }
        ImportTaskPO po = importTaskConverter.toPO(task);
        importTaskPOMapper.updateById(po);
        return task;
    }

    @Override
    public Optional<ImportTask> findById(String id) {
        log.debug("Finding import task by id: {}", id);
        ImportTaskPO po = importTaskPOMapper.selectById(id);
        return Optional.ofNullable(importTaskConverter.toEntity(po));
    }
}
