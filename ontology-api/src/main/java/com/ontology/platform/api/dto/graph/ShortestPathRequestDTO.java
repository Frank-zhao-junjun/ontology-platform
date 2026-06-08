package com.ontology.platform.api.dto.graph;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 最短路径查询请求DTO
 * Shortest Path Query Request DTO
 */
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
