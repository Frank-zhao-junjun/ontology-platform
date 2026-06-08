package com.ontology.platform.domain.vo.traversal;

import java.util.Collections;
import java.util.Map;

public record CypherQuery(
    String cypher,
    Map<String, Object> params
) {
    public CypherQuery {
        params = params != null ? Collections.unmodifiableMap(params) : Collections.emptyMap();
    }
}
