package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.ObjectTypeV2;
import java.util.List;
import java.util.Optional;

public interface ObjectTypeRepository {
    void save(ObjectTypeV2 objectType);
    Optional<ObjectTypeV2> findById(String id);
    List<ObjectTypeV2> findByContextId(String contextId);
}
