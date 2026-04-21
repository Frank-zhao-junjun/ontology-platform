package com.ontology.platform.application.service.impl;

import com.ontology.platform.application.dto.*;
import com.ontology.platform.application.service.ObjectInstanceService;
import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.ObjectInstance;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.repository.ObjectInstanceRepository;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.vo.Property;
import com.ontology.platform.domain.vo.ValidationResult;
import com.ontology.platform.infrastructure.graph.GraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 对象实例服务实现
 * ObjectInstance Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectInstanceServiceImpl implements ObjectInstanceService {

    private final ObjectInstanceRepository instanceRepository;
    private final ObjectTypeRepository objectTypeRepository;
    private final GraphService graphService;

    @Override
    @Transactional
    public InstanceResponse createInstance(String ontologyId, CreateInstanceRequest request, String userId) {
        log.info("Creating instance for ontology={}, objectType={}", ontologyId, request.getObjectTypeId());

        // 1. 获取ObjectType定义
        ObjectType objectType = objectTypeRepository.findById(request.getObjectTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", request.getObjectTypeId()));

        // 2. 验证本体匹配
        if (!objectType.getOntologyId().equals(ontologyId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "ObjectType不属于指定的本体");
        }

        // 3. 获取主键值
        String primaryKeyValue = extractPrimaryKeyValue(request.getProperties(), objectType);

        // 4. 验证主键唯一性
        if (instanceRepository.existsByOntologyIdAndObjectTypeIdAndPrimaryKeyValue(ontologyId, objectType.getId(), primaryKeyValue)) {
            throw new BusinessException(ErrorCode.DUPLICATE_ENTRY, "实例已存在: " + primaryKeyValue);
        }

        // 5. 动态Schema验证
        ValidationResult validationResult = validateInstanceProperties(request.getProperties(), objectType);
        if (!validationResult.isValid()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, 
                    "属性验证失败: " + validationResult.getErrors().stream()
                            .map(e -> e.getField() + ": " + e.getMessage())
                            .collect(Collectors.joining("; ")));
        }

        // 6. 创建实例
        ObjectInstance instance = ObjectInstance.create(
                ontologyId,
                objectType.getId(),
                objectType.getName(),
                primaryKeyValue,
                request.getProperties(),
                userId
        );

        // 7. 保存到数据库
        instance = instanceRepository.save(instance);

        // 8. 同步创建图顶点
        try {
            graphService.createVertex(instance);
        } catch (Exception e) {
            log.error("Failed to create graph vertex for instance: {}", instance.getId(), e);
            // 图数据库失败不影响主流程，仅记录日志
        }

        // 9. 更新ObjectType实例计数
        objectType.updateInstanceCount(1);
        objectTypeRepository.update(objectType);

        log.info("Instance created successfully: id={}", instance.getId());
        return toResponse(instance);
    }

    @Override
    public InstanceResponse getInstance(String ontologyId, String id) {
        log.debug("Getting instance: id={}", id);
        
        ObjectInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ObjectInstance", id));

        if (!instance.getOntologyId().equals(ontologyId)) {
            throw new ResourceNotFoundException("ObjectInstance", id);
        }

        if (instance.isDeleted()) {
            throw new ResourceNotFoundException("ObjectInstance", id);
        }

        return toResponse(instance);
    }

    @Override
    public InstanceResponse getInstanceByPrimaryKey(String ontologyId, String objectTypeId, String primaryKeyValue) {
        log.debug("Getting instance by primary key: ontology={}, type={}, pk={}", ontologyId, objectTypeId, primaryKeyValue);
        
        ObjectInstance instance = instanceRepository.findByOntologyIdAndPrimaryKeyValue(ontologyId, primaryKeyValue)
                .orElseThrow(() -> new ResourceNotFoundException("ObjectInstance", "primaryKey=" + primaryKeyValue));

        if (instance.isDeleted()) {
            throw new ResourceNotFoundException("ObjectInstance", "primaryKey=" + primaryKeyValue);
        }

        return toResponse(instance);
    }

    @Override
    public ObjectListResponse<InstanceResponse> listInstances(String ontologyId, InstanceQuery query) {
        log.debug("Listing instances: ontology={}, query={}", ontologyId, query);

        List<ObjectInstance> instances;
        long total;

        if (query.getObjectTypeId() != null) {
            instances = instanceRepository.findByOntologyIdAndObjectTypeId(ontologyId, query.getObjectTypeId());
            total = instanceRepository.countByOntologyIdAndObjectTypeId(ontologyId, query.getObjectTypeId());
        } else {
            instances = instanceRepository.findByOntologyId(ontologyId);
            total = instanceRepository.countByOntologyId(ontologyId);
        }

        // 过滤删除的实例
        instances = instances.stream()
                .filter(i -> !i.isDeleted())
                .filter(i -> query.getStatus() == null || query.getStatus().equals(i.getStatus()))
                .collect(Collectors.toList());

        // 属性过滤
        if (query.getPropertyFilters() != null && !query.getPropertyFilters().isEmpty()) {
            instances = filterByProperties(instances, query.getPropertyFilters());
        }

        // 分页
        int totalFiltered = instances.size();
        int fromIndex = (query.getPage() - 1) * query.getPageSize();
        int toIndex = Math.min(fromIndex + query.getPageSize(), instances.size());
        
        if (fromIndex >= instances.size()) {
            instances = Collections.emptyList();
        } else {
            instances = instances.subList(fromIndex, toIndex);
        }

        List<InstanceResponse> responses = instances.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        boolean hasMore = fromIndex + responses.size() < totalFiltered;

        return ObjectListResponse.<InstanceResponse>builder()
                .items(responses)
                .total(total)
                .totalFiltered(totalFiltered)
                .page(query.getPage())
                .pageSize(query.getPageSize())
                .hasMore(hasMore)
                .build();
    }

    @Override
    @Transactional
    public InstanceResponse updateInstance(String ontologyId, String id, UpdateInstanceRequest request) {
        log.info("Updating instance: id={}", id);

        ObjectInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ObjectInstance", id));

        if (!instance.getOntologyId().equals(ontologyId)) {
            throw new ResourceNotFoundException("ObjectInstance", id);
        }

        if (instance.isDeleted()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "不能更新已删除的实例");
        }

        // 获取ObjectType进行验证
        ObjectType objectType = objectTypeRepository.findById(instance.getObjectTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", instance.getObjectTypeId()));

        // 验证新属性
        if (request.getProperties() != null && !request.getProperties().isEmpty()) {
            ValidationResult validationResult = validateInstanceProperties(request.getProperties(), objectType);
            if (!validationResult.isValid()) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                        "属性验证失败: " + validationResult.getErrors().stream()
                                .map(e -> e.getField() + ": " + e.getMessage())
                                .collect(Collectors.joining("; ")));
            }

            // 更新属性
            instance.update(request.getProperties());
        }

        // 保存更新
        instance = instanceRepository.update(instance);

        // 同步更新图顶点
        try {
            graphService.updateVertex(instance);
        } catch (Exception e) {
            log.error("Failed to update graph vertex for instance: {}", instance.getId(), e);
        }

        log.info("Instance updated successfully: id={}", instance.getId());
        return toResponse(instance);
    }

    @Override
    @Transactional
    public void deleteInstance(String ontologyId, String id) {
        log.info("Deleting instance: id={}", id);

        ObjectInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ObjectInstance", id));

        if (!instance.getOntologyId().equals(ontologyId)) {
            throw new ResourceNotFoundException("ObjectInstance", id);
        }

        // 逻辑删除
        instance.markAsDeleted();
        instanceRepository.update(instance);

        // 同步删除图顶点
        try {
            graphService.deleteVertex(instance);
        } catch (Exception e) {
            log.error("Failed to delete graph vertex for instance: {}", instance.getId(), e);
        }

        // 更新ObjectType实例计数
        ObjectType objectType = objectTypeRepository.findById(instance.getObjectTypeId())
                .orElse(null);
        if (objectType != null) {
            objectType.updateInstanceCount(-1);
            objectTypeRepository.update(objectType);
        }

        log.info("Instance deleted successfully: id={}", id);
    }

    @Override
    public ObjectListResponse<InstanceResponse> listInstancesByType(String ontologyId, String typeId) {
        log.debug("Listing instances by type: ontology={}, typeId={}", ontologyId, typeId);
        
        List<ObjectInstance> instances = instanceRepository.findByOntologyIdAndObjectTypeId(ontologyId, typeId);
        
        // 过滤删除的实例
        instances = instances.stream()
                .filter(i -> !i.isDeleted())
                .collect(Collectors.toList());

        List<InstanceResponse> responses = instances.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ObjectListResponse.<InstanceResponse>builder()
                .items(responses)
                .total(instances.size())
                .totalFiltered(instances.size())
                .page(1)
                .pageSize(instances.size())
                .hasMore(false)
                .build();
    }

    @Override
    @Transactional
    public BatchImportResponse batchImport(String ontologyId, BatchImportRequest request, String userId) {
        log.info("Batch importing instances: ontology={}, objectType={}, count={}", 
                ontologyId, request.getObjectTypeId(), request.getInstances().size());

        ObjectType objectType = objectTypeRepository.findById(request.getObjectTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", request.getObjectTypeId()));

        if (!objectType.getOntologyId().equals(ontologyId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "ObjectType不属于指定的本体");
        }

        List<ObjectInstance> toSave = new ArrayList<>();
        List<BatchImportResponse.FailedRecord> failedRecords = new ArrayList<>();
        List<InstanceResponse> importedInstances = new ArrayList<>();
        int skippedCount = 0;

        for (BatchImportRequest.InstanceData data : request.getInstances()) {
            try {
                // 检查是否已存在
                if (instanceRepository.existsByOntologyIdAndObjectTypeIdAndPrimaryKeyValue(
                        ontologyId, objectType.getId(), data.getPrimaryKeyValue())) {
                    if (request.isSkipExisting()) {
                        skippedCount++;
                        continue;
                    } else {
                        failedRecords.add(BatchImportResponse.FailedRecord.builder()
                                .primaryKeyValue(data.getPrimaryKeyValue())
                                .errorMessage("实例已存在")
                                .build());
                        continue;
                    }
                }

                // 验证属性
                ValidationResult validationResult = validateInstanceProperties(data.getProperties(), objectType);
                if (!validationResult.isValid()) {
                    failedRecords.add(BatchImportResponse.FailedRecord.builder()
                            .primaryKeyValue(data.getPrimaryKeyValue())
                            .errorMessage("属性验证失败: " + validationResult.getErrors().stream()
                                    .map(e -> e.getField() + ": " + e.getMessage())
                                    .collect(Collectors.joining("; ")))
                            .build());
                    continue;
                }

                // 创建实例
                ObjectInstance instance = ObjectInstance.create(
                        ontologyId,
                        objectType.getId(),
                        objectType.getName(),
                        data.getPrimaryKeyValue(),
                        data.getProperties(),
                        userId
                );
                toSave.add(instance);

            } catch (Exception e) {
                log.error("Failed to prepare instance for import: {}", data.getPrimaryKeyValue(), e);
                failedRecords.add(BatchImportResponse.FailedRecord.builder()
                        .primaryKeyValue(data.getPrimaryKeyValue())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        // 批量保存
        List<ObjectInstance> saved = instanceRepository.saveAll(toSave);
        
        // 同步图顶点
        for (ObjectInstance instance : saved) {
            try {
                graphService.createVertex(instance);
            } catch (Exception e) {
                log.error("Failed to create graph vertex for imported instance: {}", instance.getId(), e);
            }
            importedInstances.add(toResponse(instance));
        }

        // 更新实例计数
        if (!saved.isEmpty()) {
            objectType.updateInstanceCount(saved.size());
            objectTypeRepository.update(objectType);
        }

        return BatchImportResponse.builder()
                .total(request.getInstances().size())
                .successCount(saved.size())
                .failureCount(failedRecords.size())
                .skippedCount(skippedCount)
                .failedRecords(failedRecords)
                .importedInstances(importedInstances)
                .build();
    }

    @Override
    public void batchDelete(String ontologyId, List<String> instanceIds) {
        log.info("Batch deleting instances: ontology={}, count={}", ontologyId, instanceIds.size());

        for (String id : instanceIds) {
            try {
                deleteInstance(ontologyId, id);
            } catch (Exception e) {
                log.error("Failed to delete instance: {}", id, e);
            }
        }
    }

    @Override
    public InstanceValidationResponse validateInstance(String ontologyId, String id) {
        ObjectInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ObjectInstance", id));

        if (!instance.getOntologyId().equals(ontologyId)) {
            throw new ResourceNotFoundException("ObjectInstance", id);
        }

        ObjectType objectType = objectTypeRepository.findById(instance.getObjectTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ObjectType", instance.getObjectTypeId()));

        ValidationResult result = validateInstanceProperties(instance.getProperties(), objectType);

        return buildValidationResponse(List.of(instance), result.hasErrors() ? result : null);
    }

    @Override
    public InstanceValidationResponse validateInstances(String ontologyId, List<String> instanceIds, boolean strictMode) {
        List<ObjectInstance> instances = instanceIds.stream()
                .map(id -> instanceRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .filter(i -> i.getOntologyId().equals(ontologyId))
                .collect(Collectors.toList());

        List<InstanceValidationResponse.InstanceValidationResult> results = new ArrayList<>();

        for (ObjectInstance instance : instances) {
            ObjectType objectType = objectTypeRepository.findById(instance.getObjectTypeId()).orElse(null);
            ValidationResult vr;
            
            if (objectType != null) {
                vr = validateInstanceProperties(instance.getProperties(), objectType);
            } else {
                vr = ValidationResult.failure("objectType", "对象类型不存在");
            }

            List<InstanceValidationResponse.ValidationIssue> issues = new ArrayList<>();
            for (ValidationResult.ValidationError error : vr.getErrors()) {
                issues.add(InstanceValidationResponse.ValidationIssue.builder()
                        .severity(error.getSeverity())
                        .type("VALIDATION_ERROR")
                        .field(error.getField())
                        .message(error.getMessage())
                        .build());
            }
            for (ValidationResult.ValidationError warning : vr.getWarnings()) {
                issues.add(InstanceValidationResponse.ValidationIssue.builder()
                        .severity(warning.getSeverity())
                        .type("VALIDATION_WARNING")
                        .field(warning.getField())
                        .message(warning.getMessage())
                        .build());
            }

            if (strictMode && !issues.isEmpty()) {
                issues.add(InstanceValidationResponse.ValidationIssue.builder()
                        .severity("ERROR")
                        .type("STRICT_MODE_VIOLATION")
                        .field("")
                        .message("严格模式下验证未通过")
                        .build());
            }

            results.add(InstanceValidationResponse.InstanceValidationResult.builder()
                    .instanceId(instance.getId())
                    .primaryKeyValue(instance.getPrimaryKeyValue())
                    .valid(vr.isValid() && (!strictMode || issues.isEmpty()))
                    .issues(issues)
                    .build());
        }

        int errorCount = results.stream().mapToInt(r -> (int) r.getIssues().stream()
                .filter(i -> "ERROR".equals(i.getSeverity())).count()).sum();
        int warningCount = results.stream().mapToInt(r -> (int) r.getIssues().stream()
                .filter(i -> "WARNING".equals(i.getSeverity())).count()).sum();

        return InstanceValidationResponse.builder()
                .valid(results.stream().allMatch(InstanceValidationResponse.InstanceValidationResult::isValid))
                .instanceCount(instances.size())
                .validCount((int) results.stream().filter(InstanceValidationResponse.InstanceValidationResult::isValid).count())
                .invalidCount((int) results.stream().filter(r -> !r.isValid()).count())
                .errorCount(errorCount)
                .warningCount(warningCount)
                .results(results)
                .build();
    }

    @Override
    @Transactional
    public InstanceResponse activateInstance(String ontologyId, String id) {
        ObjectInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ObjectInstance", id));

        if (!instance.getOntologyId().equals(ontologyId)) {
            throw new ResourceNotFoundException("ObjectInstance", id);
        }

        instance.activate();
        instance = instanceRepository.update(instance);

        return toResponse(instance);
    }

    @Override
    @Transactional
    public InstanceResponse deactivateInstance(String ontologyId, String id) {
        ObjectInstance instance = instanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ObjectInstance", id));

        if (!instance.getOntologyId().equals(ontologyId)) {
            throw new ResourceNotFoundException("ObjectInstance", id);
        }

        instance.deactivate();
        instance = instanceRepository.update(instance);

        return toResponse(instance);
    }

    // ==================== 私有方法 ====================

    /**
     * 从属性中提取主键值
     */
    private String extractPrimaryKeyValue(Map<String, Object> properties, ObjectType objectType) {
        String primaryKey = objectType.getPrimaryKey();
        if (primaryKey == null || primaryKey.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "ObjectType未定义主键");
        }

        Object value = properties.get(primaryKey);
        if (value == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "缺少主键属性: " + primaryKey);
        }

        return String.valueOf(value);
    }

    /**
     * 验证实例属性是否符合ObjectType定义
     */
    private ValidationResult validateInstanceProperties(Map<String, Object> properties, ObjectType objectType) {
        ValidationResult result = ValidationResult.success();

        if (objectType.getProperties() == null || objectType.getProperties().isEmpty()) {
            return result;
        }

        for (Property propDef : objectType.getProperties()) {
            String propName = propDef.getName();
            Object propValue = properties.get(propName);

            // 必填属性检查
            if (propDef.isRequired() && (propValue == null || isEmptyValue(propValue))) {
                result.addError(propName, "必填属性不能为空");
                continue;
            }

            // 非空值才进行类型和约束校验
            if (propValue != null && !isEmptyValue(propValue)) {
                // 数据类型校验
                if (!propDef.validateValue(propValue)) {
                    result.addError(propName, "数据类型错误，期望: " + propDef.getDataType().getDescription());
                }

                // 约束规则校验
                validateConstraints(result, propName, propValue, propDef);
            }
        }

        return result;
    }

    /**
     * 验证约束规则
     */
    private void validateConstraints(ValidationResult result, String propName, Object propValue, Property propDef) {
        // 这里可以实现更多的约束校验
        // 如: min/max, pattern, enum等
        // 当前基于Property类的validateValue方法进行基础类型校验
    }

    /**
     * 检查值是否为空
     */
    private boolean isEmptyValue(Object value) {
        if (value == null) return true;
        if (value instanceof String) return ((String) value).isBlank();
        if (value instanceof Collection) return ((Collection<?>) value).isEmpty();
        if (value instanceof Map) return ((Map<?, ?>) value).isEmpty();
        return false;
    }

    /**
     * 根据属性过滤
     */
    private List<ObjectInstance> filterByProperties(List<ObjectInstance> instances, Map<String, Object> filters) {
        return instances.stream()
                .filter(instance -> {
                    for (Map.Entry<String, Object> entry : filters.entrySet()) {
                        Object propValue = instance.getProperty(entry.getKey());
                        if (!Objects.equals(propValue, entry.getValue())) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    /**
     * 转换为响应DTO
     */
    private InstanceResponse toResponse(ObjectInstance instance) {
        return InstanceResponse.builder()
                .id(instance.getId())
                .ontologyId(instance.getOntologyId())
                .objectTypeId(instance.getObjectTypeId())
                .objectTypeName(instance.getObjectTypeName())
                .primaryKeyValue(instance.getPrimaryKeyValue())
                .properties(instance.getProperties())
                .status(instance.getStatus())
                .version(instance.getVersion())
                .createdAt(instance.getCreatedAt())
                .updatedAt(instance.getUpdatedAt())
                .createdBy(instance.getCreatedBy())
                .build();
    }

    /**
     * 构建验证响应
     */
    private InstanceValidationResponse buildValidationResponse(List<ObjectInstance> instances, ValidationResult overallResult) {
        List<InstanceValidationResponse.InstanceValidationResult> results = instances.stream()
                .map(inst -> InstanceValidationResponse.InstanceValidationResult.builder()
                        .instanceId(inst.getId())
                        .primaryKeyValue(inst.getPrimaryKeyValue())
                        .valid(true)
                        .issues(Collections.emptyList())
                        .build())
                .collect(Collectors.toList());

        int errorCount = overallResult != null ? overallResult.getErrors().size() : 0;
        int warningCount = overallResult != null ? overallResult.getWarnings().size() : 0;

        return InstanceValidationResponse.builder()
                .valid(overallResult == null || overallResult.isValid())
                .instanceCount(instances.size())
                .validCount(overallResult != null && overallResult.isValid() ? instances.size() : 0)
                .invalidCount(overallResult != null && !overallResult.isValid() ? instances.size() : 0)
                .errorCount(errorCount)
                .warningCount(warningCount)
                .results(results)
                .build();
    }
}
