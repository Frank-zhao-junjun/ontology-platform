package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.*;
import java.util.List;

public interface DomainQueryService {
    List<ActionDefinitionResponse> queryActions(String ontologyId, String entityId);
    List<EventDefinitionResponse> queryEvents(String ontologyId, String entityId);
    List<EpcStepResponse> queryEpc(String ontologyId, String flowName);
}
