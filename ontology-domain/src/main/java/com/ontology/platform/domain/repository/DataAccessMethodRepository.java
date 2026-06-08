package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.DataAccessMethod;
import java.util.Optional;

public interface DataAccessMethodRepository {
    void save(DataAccessMethod method);
    boolean existsByObjectTypeAndSourceAndMethod(String objectTypeId, String dataSourceId, String methodType);
}
