package com.ontology.platform.domain.repository;

import com.ontology.platform.domain.entity.AggregateRoot;
import java.util.*;

public interface AggregateRootRepository {
    void save(AggregateRoot ar);
    Optional<AggregateRoot> findById(String id);
    List<AggregateRoot> findByContextId(String contextId);
    boolean existsByCode(String contextId, String code);
}
