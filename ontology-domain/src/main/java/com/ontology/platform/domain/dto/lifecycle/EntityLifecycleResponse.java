package com.ontology.platform.domain.dto.lifecycle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntityLifecycleResponse {
    private String ontologyId;
    private String entityId;
    private String snapshotVersion;
    private Map<String, Object> lifecycleData;
}
