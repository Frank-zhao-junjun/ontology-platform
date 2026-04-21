package com.ontology.platform.api.dto.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 子图提取请求DTO
 * Subgraph Extraction Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubgraphRequestDTO {
    
    private String rootObjectId;
    
    @Builder.Default
    private int depth = 3;
}
