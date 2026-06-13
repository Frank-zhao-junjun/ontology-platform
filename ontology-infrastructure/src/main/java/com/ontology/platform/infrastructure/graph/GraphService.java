package com.ontology.platform.infrastructure.graph;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GraphService {
    public void createEdge(String sourceId, String targetId, String relationName) {
        log.debug("Skipping graph edge creation in placeholder service: {} -[{}]-> {}", sourceId, relationName, targetId);
    }

    public void deleteEdge(String sourceId, String targetId, String relationName) {
        log.debug("Skipping graph edge deletion in placeholder service: {} -[{}]-> {}", sourceId, relationName, targetId);
    }
}
