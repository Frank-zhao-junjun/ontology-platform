package com.ontology.platform.api.dto;

import com.ontology.platform.domain.entity.AggregateRoot;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data @Builder
public class AggregateRootResponse {
    private String id, contextId, name, code, description;
    private boolean active;
    private Instant createdAt;
    public static AggregateRootResponse from(AggregateRoot ar) { return builder().id(ar.getId()).contextId(ar.getContextId()).name(ar.getName()).code(ar.getCode()).description(ar.getDescription()).active(ar.isActive()).createdAt(ar.getCreatedAt()).build(); }
}
