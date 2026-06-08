package com.ontology.platform.infrastructure.persistence;

import com.ontology.platform.domain.repository.ActionRuleLinkRepository;
import com.ontology.platform.infrastructure.persistence.entity.ActionRuleLinkEntity;
import com.ontology.platform.infrastructure.repository.ActionRuleLinkJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaActionRuleLinkRepository implements ActionRuleLinkRepository {
    private final ActionRuleLinkJpaRepository jpa;

    @Override
    public void replaceLinks(String actionId, List<String> ruleIdsInOrder) {
        jpa.deleteByActionId(actionId);
        if (ruleIdsInOrder == null) {
            return;
        }
        for (int i = 0; i < ruleIdsInOrder.size(); i++) {
            ActionRuleLinkEntity link = new ActionRuleLinkEntity();
            link.setActionId(actionId);
            link.setRuleId(ruleIdsInOrder.get(i));
            link.setSortOrder(i);
            jpa.save(link);
        }
    }

    @Override
    public List<String> findRuleIdsByActionId(String actionId) {
        return jpa.findByActionIdOrderBySortOrderAsc(actionId).stream()
                .map(ActionRuleLinkEntity::getRuleId)
                .collect(Collectors.toList());
    }
}
