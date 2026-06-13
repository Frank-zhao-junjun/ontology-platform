package com.ontology.platform.domain.repository.behavior;

import com.ontology.platform.domain.entity.ActionDefinition;
import java.util.List;
import java.util.Optional;

public interface ActionDefinitionRepository {
    Optional<ActionDefinition> findById(String id);
    List<ActionDefinition> findByOntologyId(String ontologyId);
    List<ActionDefinition> findByOntologyIdAndEntityId(String ontologyId, String entityId);
    List<ActionDefinition> findByOntologyIdAndDomain(String ontologyId, String domain);
    ActionDefinition save(ActionDefinition entity);
    void deleteById(String id);
}
