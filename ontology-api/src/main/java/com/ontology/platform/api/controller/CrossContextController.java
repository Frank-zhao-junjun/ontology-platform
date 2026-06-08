package com.ontology.platform.api.controller;

import com.ontology.platform.api.dto.RelationshipResponse;
import com.ontology.platform.application.service.ModelingService;
import com.ontology.platform.infrastructure.persistence.JpaRelationshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * S09: Cross-context relationship management.
 * Existing relationships table has is_cross_context + target_context_id fields.
 * This controller provides dedicated endpoints for cross-context queries and dependency graph.
 */
@RestController
@RequiredArgsConstructor
public class CrossContextController {
    private final ModelingService modelingService;
    private final JpaRelationshipRepository relationshipRepo;

    /**
     * List all cross-context relationships globally.
     */
    @GetMapping("/v1/cross-context-relationships")
    public ResponseEntity<List<Map<String, Object>>> listAllCrossContext() {
        List<Map<String, Object>> list = relationshipRepo.findAllCrossContext().stream()
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", r.getId());
                    m.put("contextId", r.getContextId());
                    m.put("sourceObjectId", r.getSourceObjectId());
                    m.put("targetObjectId", r.getTargetObjectId());
                    m.put("name", r.getName());
                    m.put("code", r.getCode());
                    m.put("cardinality", r.getCardinality());
                    m.put("relationKind", r.getRelationKind());
                    m.put("crossContext", r.isCrossContext());
                    m.put("targetContextId", r.getTargetContextId());
                    m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
                    return m;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * List cross-context relationships for a specific context.
     */
    @GetMapping("/v1/contexts/{contextId}/cross-context-relationships")
    public ResponseEntity<List<Map<String, Object>>> listByContext(@PathVariable String contextId) {
        List<Map<String, Object>> list = relationshipRepo.findBySourceContextId(contextId).stream()
                .map(r -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", r.getId());
                    m.put("contextId", r.getContextId());
                    m.put("sourceObjectId", r.getSourceObjectId());
                    m.put("targetObjectId", r.getTargetObjectId());
                    m.put("name", r.getName());
                    m.put("code", r.getCode());
                    m.put("cardinality", r.getCardinality());
                    m.put("relationKind", r.getRelationKind());
                    m.put("crossContext", r.isCrossContext());
                    m.put("targetContextId", r.getTargetContextId());
                    m.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
                    return m;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * Global cross-context dependency graph.
     * Returns nodes (contexts) and edges (cross-context relationships).
     * Used for visualization: dashed arrows between contexts labeled with relation type.
     */
    @GetMapping("/v1/cross-context-dependency-graph")
    public ResponseEntity<Map<String, Object>> getDependencyGraph() {
        var relationships = relationshipRepo.findAllCrossContext();
        Set<String> contextIds = new LinkedHashSet<>();
        List<Map<String, Object>> edges = new ArrayList<>();

        for (var r : relationships) {
            contextIds.add(r.getContextId());
            if (r.getTargetContextId() != null) contextIds.add(r.getTargetContextId());
            Map<String, Object> edge = new LinkedHashMap<>();
            edge.put("sourceContextId", r.getContextId());
            edge.put("targetContextId", r.getTargetContextId());
            edge.put("relationType", r.getRelationKind());
            edge.put("name", r.getName());
            edge.put("code", r.getCode());
            edges.add(edge);
        }

        Map<String, Object> graph = new LinkedHashMap<>();
        graph.put("contextIds", new ArrayList<>(contextIds));
        graph.put("edges", edges);
        graph.put("totalEdges", edges.size());
        return ResponseEntity.ok(graph);
    }
}
