package com.ontology.platform.domain.repository;
import com.ontology.platform.domain.entity.BoundedContext;
import java.util.*;

public interface BoundedContextRepository {
    void save(BoundedContext ctx);
    Optional<BoundedContext> findById(String id);
    Optional<BoundedContext> findByCode(String code);
    List<BoundedContext> findAll();
    boolean existsByCode(String code);
    void update(BoundedContext ctx);
}
