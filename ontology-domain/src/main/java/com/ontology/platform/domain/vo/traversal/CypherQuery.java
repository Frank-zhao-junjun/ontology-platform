package com.ontology.platform.domain.vo.traversal;

import java.util.Map;

/**
 * Parameterized Cypher query generated from a graph traversal request.
 */
public record CypherQuery(
        String cypher,
        Map<String, Object> params
) {
}
