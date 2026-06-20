package com.ontology.platform.api.dto.graph;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 子图提取请求DTO
 * Subgraph Extraction Request DTO
 */
@Schema(description = "子图提取请求DTO，用于提取指定根节点周围的子图")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubgraphRequestDTO {
    
    private String rootObjectId;
    
    @Builder.Default
    private int depth = 3;
}
