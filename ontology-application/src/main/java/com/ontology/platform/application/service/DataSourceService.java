package com.ontology.platform.application.service;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.DataAccessMethod;
import com.ontology.platform.domain.entity.DataSource;
import com.ontology.platform.domain.repository.DataAccessMethodRepository;
import com.ontology.platform.domain.repository.DataSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DataSourceService {
    private static final Set<String> SOURCE_TYPES = Set.of("SQL", "API", "MCP");
    private static final Set<String> METHOD_TYPES = Set.of("SQL_QUERY", "API_CALL", "MCP_TOOL");

    private final ModelingService modelingService;
    private final DataSourceRepository dataSourceRepo;
    private final DataAccessMethodRepository accessMethodRepo;

    public DataSource createDataSource(String name, String code, String sourceType,
                                       String connectionConfig, String credentialRef) {
        validateSourceType(sourceType);
        if (dataSourceRepo.existsByCode(code))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "数据源 code '" + code + "' 已存在");
        DataSource ds = DataSource.create(name, code, sourceType, connectionConfig, credentialRef);
        dataSourceRepo.save(ds);
        return ds;
    }

    public List<DataSource> listDataSources(String sourceType) {
        return dataSourceRepo.findAll().stream()
                .filter(d -> sourceType == null || sourceType.isBlank() || d.getSourceType().equals(sourceType))
                .collect(Collectors.toList());
    }

    public DataSource getDataSource(String id) {
        return dataSourceRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("DataSource not found: " + id));
    }

    public DataAccessMethod createDataAccessMethod(String contextId, String objectTypeId, String dataSourceId,
                                                   String methodType, String accessConfig, int cacheTtlSec) {
        validateMethodType(methodType);
        var ot = modelingService.getObjectType(objectTypeId);
        if (!ot.getContextId().equals(contextId))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "对象类型不属于该上下文");
        getDataSource(dataSourceId);
        if (accessMethodRepo.existsByObjectTypeAndSourceAndMethod(objectTypeId, dataSourceId, methodType))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "同一对象类型、数据源与方法类型已存在");
        DataAccessMethod m = DataAccessMethod.create(contextId, objectTypeId, dataSourceId, methodType, accessConfig, cacheTtlSec);
        accessMethodRepo.save(m);
        return m;
    }

    private static void validateSourceType(String sourceType) {
        if (sourceType == null || !SOURCE_TYPES.contains(sourceType))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "sourceType 须为 SQL | API | MCP");
    }

    private static void validateMethodType(String methodType) {
        if (methodType == null || !METHOD_TYPES.contains(methodType))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "methodType 须为 SQL_QUERY | API_CALL | MCP_TOOL");
    }
}
