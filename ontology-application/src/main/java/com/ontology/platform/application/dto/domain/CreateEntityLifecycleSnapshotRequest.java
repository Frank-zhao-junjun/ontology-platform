package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建EntityLifecycleSnapshot请求")
public class CreateEntityLifecycleSnapshotRequest {
    @Schema(description = "entity_id") private String entityId;
    @Schema(description = "ontology_id") private String ontologyId;
    @Schema(description = "lifecycle_data") private String lifecycleData;
    @Schema(description = "snapshot_version") private String snapshotVersion;
}
