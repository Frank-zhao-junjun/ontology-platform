package com.ontology.platform.application.dto.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Schema(description = "创建EpcNode请求")
public class CreateEpcNodeRequest {
    @Schema(description = "chain_id") private String chainId;
    @Schema(description = "node_type") private String nodeType;
    @Schema(description = "name") private String name;
    @Schema(description = "description") private String description;
    @Schema(description = "ref_type") private String refType;
    @Schema(description = "ref_id") private String refId;
    @Schema(description = "metadata") private String metadata;
    @Schema(description = "sort_order") private Integer sortOrder;
}
