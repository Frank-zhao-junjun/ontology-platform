package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "EntityLifecycleSnapshot响应")
public class EntityLifecycleSnapshotResponse {
    @Schema(description = "ID") private String id;
    @Schema(description = "entity_id") private String entityId;
    @Schema(description = "ontology_id") private String ontologyId;
    @Schema(description = "lifecycle_data") private String lifecycleData;
    @Schema(description = "snapshot_version") private String snapshotVersion;
    @Schema(description = "创建时间") private Instant createdAt;
}
