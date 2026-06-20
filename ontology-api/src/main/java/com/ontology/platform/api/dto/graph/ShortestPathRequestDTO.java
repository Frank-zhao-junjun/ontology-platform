package com.ontology.platform.api.dto.graph;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 最短路径查询请求DTO
 * Shortest Path Query Request DTO
 */
@Schema(description = "最短路径查询请求DTO，用于查询图中两节点间的最短路径")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortestPathRequestDTO {
    
    private String fromObjectId;
    private String toObjectId;
    
    @Builder.Default
    private int maxDepth = 5;
}
