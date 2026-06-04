package com.ontology.platform.application.service;

import com.ontology.platform.common.enums.DomainTag;
import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.BoundedContext;
import com.ontology.platform.domain.repository.BoundedContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BoundedContextService {
    private final BoundedContextRepository repository;

    public BoundedContext create(String name, String code, String description, DomainTag domainTag, String createdBy) {
        return create(name, code, description, domainTag, createdBy, null);
    }

    public BoundedContext create(String name, String code, String description, DomainTag domainTag,
                                 String createdBy, String ontologyId) {
        if (repository.existsByCode(code))
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Code '" + code + "' already exists");
        BoundedContext ctx = BoundedContext.create(name, code, description, domainTag, createdBy, ontologyId);
        repository.save(ctx);
        return ctx;
    }

    public BoundedContext findById(String id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Context not found: " + id)); }

    public BoundedContext submitForReview(String id) { BoundedContext c = findById(id); c.submitForReview(); repository.update(c); return c; }

    public BoundedContext approveAndPublish(String id) { BoundedContext c = findById(id); c.approveAndPublish(); repository.update(c); return c; }

    public BoundedContext rejectToDraft(String id) { BoundedContext c = findById(id); c.rejectToDraft(); repository.update(c); return c; }

    public List<BoundedContext> findAll() { return repository.findAll(); }
}
