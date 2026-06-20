package com.ontology.platform.api.dto.graph;

import com.ontology.platform.common.enums.ReturnFormat;
import com.ontology.platform.common.enums.TraversalDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图遍历请求DTO
 * Graph Traversal Request DTO
 */
@Schema(description = "图遍历请求DTO，用于指定遍历起点、路径条件和最大深度")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraphTraversalRequestDTO {
    
    @NotBlank(message = "startObjectType is required")
    private String startObjectType;
    
    @NotBlank(message = "startObjectId is required")
    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "startObjectId must be a valid UUID")
    private String startObjectId;
    
    private List<TraversalPathDTO> path;
    
    @Min(value = 1, message = "maxDepth must be at least 1")
    @Max(value = 5, message = "maxDepth cannot exceed 5")
    @Builder.Default
    private int maxDepth = 3;
    
    @Builder.Default
    private TraversalDirection direction = TraversalDirection.OUTGOING;
    
    @Min(value = 1, message = "limit must be at least 1")
    @Max(value = 1000, message = "limit cannot exceed 1000")
    @Builder.Default
    private int limit = 100;
    
    private List<TraversalFilterDTO> filters;
    
    @Builder.Default
    private ReturnFormat returnFormat = ReturnFormat.GRAPH;
    
    private List<String> includeProperties;
    
    private List<String> excludeProperties;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TraversalPathDTO {
        
        @NotBlank(message = "relationType is required")
        private String relationType;
        
        private String targetObjectType;
        
        @Min(value = 1, message = "depth must be at least 1")
        @Max(value = 3, message = "depth cannot exceed 3")
        @Builder.Default
        private int depth = 1;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TraversalFilterDTO {
        
        @Min(value = 0, message = "depth must be non-negative")
        @Builder.Default
        private int depth = 0;
        
        private String targetType;
        
        private List<FilterConditionDTO> conditions;
        
        @Builder.Default
        private String logic = "AND";
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterConditionDTO {
        
        @NotBlank(message = "field is required")
        @Pattern(regexp = "^[a-zA-Z_][a-zA-Z0-9_]*$", 
                message = "field must match pattern: ^[a-zA-Z_][a-zA-Z0-9_]*$")
        private String field;
        
        @NotBlank(message = "operator is required")
        private String operator;
        
        private Object value;
    }
}
