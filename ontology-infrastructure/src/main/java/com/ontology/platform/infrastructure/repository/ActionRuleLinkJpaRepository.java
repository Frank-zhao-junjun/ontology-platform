package com.ontology.platform.infrastructure.repository;

import com.ontology.platform.infrastructure.persistence.entity.ActionRuleLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionRuleLinkJpaRepository extends JpaRepository<ActionRuleLinkEntity, ActionRuleLinkEntity.Key> {
    List<ActionRuleLinkEntity> findByActionIdOrderBySortOrderAsc(String actionId);
    void deleteByActionId(String actionId);
}
