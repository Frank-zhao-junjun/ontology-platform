package com.ontology.platform.domain.repository;

import java.util.List;

public interface ActionRuleLinkRepository {
    void replaceLinks(String actionId, List<String> ruleIdsInOrder);
    List<String> findRuleIdsByActionId(String actionId);
}
