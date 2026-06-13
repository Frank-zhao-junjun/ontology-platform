package com.ontology.platform.application.security;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.ValidationException;
import com.ontology.platform.domain.service.GraphWhitelistService;
import com.ontology.platform.domain.vo.traversal.CypherQuery;
import com.ontology.platform.domain.vo.traversal.GraphTraversalRequest;
import com.ontology.platform.domain.vo.traversal.TraversalFilter;
import com.ontology.platform.domain.vo.traversal.TraversalFilterCondition;
import com.ontology.platform.domain.vo.traversal.TraversalPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 图遍历DSL解析器
 * Graph Traversal DSL Parser
 * 
 * 参考TDD 4.8.3.3设计，将结构化请求转换为安全的Cypher查询
 * 
 * 安全措施：
 * 1. 结构化DSL转Cypher
 * 2. 白名单校验
 * 3. 参数化查询
 * 4. 深度限制
 * 5. 结果限制
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraphTraversalDSLParser {
    
    // ==================== 依赖 ====================
    
    private final FilterSecurityValidator filterValidator;
    private final GraphWhitelistService whitelistService;
    
    // ==================== 安全常量 ====================
    
    /** 最大深度限制 */
    private static final int MAX_DEPTH = 5;
    
    /** 最大结果限制 */
    private static final int MAX_LIMIT = 1000;
    
    // ==================== 解析结果 ====================
    
    /**
     * Cypher解析结果
     */
    public record CypherQuery(
        String cypher,          // Cypher查询语句
        Map<String, Object> params  // 参数化参数
    ) {}
    
    // ==================== 公开方法 ====================
    
    /**
     * 解析图遍历请求为Cypher查询
     * @param request 图遍历请求
     * @return Cypher查询和参数
     */
    public com.ontology.platform.domain.vo.traversal.CypherQuery parse(GraphTraversalRequest request) {
        log.debug("Parsing graph traversal request: startObjectType={}, startObjectId={}", 
            request.getStartObjectType(), request.getStartObjectId());
        
        // 1. 验证必填字段
        validateRequiredFields(request);
        
        // 2. 验证并规范化起点
        String startObjectType = whitelistService.normalizeObjectType(request.getStartObjectType());
        validateObjectType(startObjectType);
        
        // 3. 验证并规范化路径
        List<TraversalPath> validatedPath = validateAndNormalizePath(request.getPath());
        
        // 4. 验证深度限制
        int depth = Math.min(request.getEffectiveDepth(), MAX_DEPTH);
        
        // 5. 验证结果数量限制
        int limit = Math.min(request.getEffectiveLimit(), MAX_LIMIT);
        
        // 6. 验证过滤条件
        List<TraversalFilter> validatedFilters = filterValidator.validateFilters(request.getFilters());
        
        // 7. 生成参数化Cypher
        return buildCypher(request, validatedPath, depth, limit, validatedFilters);
    }
    
    // ==================== 验证方法 ====================
    
    /**
     * 验证必填字段
     */
    private void validateRequiredFields(GraphTraversalRequest request) {
        if (request.getStartObjectType() == null || request.getStartObjectType().isBlank()) {
            throw new ValidationException(ErrorCode.INVALID_TRAVERSAL_REQUEST, "startObjectType is required");
        }
        
        if (request.getStartObjectId() == null || request.getStartObjectId().isBlank()) {
            throw new ValidationException(ErrorCode.INVALID_TRAVERSAL_REQUEST, "startObjectId is required");
        }
        
        // UUID格式校验
        if (!isValidUUID(request.getStartObjectId())) {
            throw new ValidationException(ErrorCode.INVALID_TRAVERSAL_REQUEST, 
                "startObjectId must be a valid UUID format");
        }
    }
    
    /**
     * 验证对象类型
     */
    private void validateObjectType(String objectType) {
        if (!whitelistService.isObjectTypeAllowed(objectType)) {
            throw new ValidationException(ErrorCode.INVALID_OBJECT_TYPE,
                String.format("Object type '%s' is not allowed", objectType));
        }
    }
    
    /**
     * 验证并规范化路径
     */
    private List<TraversalPath> validateAndNormalizePath(List<TraversalPath> path) {
        if (path == null || path.isEmpty()) {
            return List.of();
        }
        
        List<TraversalPath> validated = new ArrayList<>();
        for (TraversalPath segment : path) {
            if (segment == null || segment.getRelationType() == null) {
                continue;
            }
            
            // 规范化关系类型
            String relationType = whitelistService.normalizeRelationType(segment.getRelationType());
            
            // 白名单验证
            if (!whitelistService.isRelationTypeAllowed(relationType)) {
                throw new ValidationException(ErrorCode.INVALID_RELATION_TYPE,
                    String.format("Relation type '%s' is not allowed", relationType));
            }
            
            // 验证目标对象类型
            String targetType = segment.getTargetObjectType();
            if (targetType != null && !targetType.isBlank()) {
                targetType = whitelistService.normalizeObjectType(targetType);
                if (!whitelistService.isObjectTypeAllowed(targetType)) {
                    throw new ValidationException(ErrorCode.INVALID_OBJECT_TYPE,
                        String.format("Target object type '%s' is not allowed", targetType));
                }
            }
            
            validated.add(TraversalPath.builder()
                    .relationType(relationType)
                    .targetObjectType(targetType)
                    .depth(Math.max(1, Math.min(segment.getDepth(), 3)))
                    .build());
        }
        
        return validated;
    }
    
    // ==================== Cypher构建方法 ====================
    
    /**
     * 构建参数化Cypher查询
     */
    private com.ontology.platform.domain.vo.traversal.CypherQuery buildCypher(
            GraphTraversalRequest request,
            List<TraversalPath> validatedPath,
            int depth,
            int limit,
            List<TraversalFilter> validatedFilters) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("startObjectId", request.getStartObjectId());
        params.put("startObjectType", whitelistService.normalizeObjectType(request.getStartObjectType()));
        params.put("maxDepth", depth);
        params.put("limit", limit);
        
        StringBuilder cypher = new StringBuilder();
        
        // MATCH子句
        cypher.append("MATCH path = (start:Object {");
        cypher.append("id: $startObjectId, ");
        cypher.append("objectType: $startObjectType");
        cypher.append("})");
        
        // 遍历路径构建
        String traversalPattern = buildTraversalPattern(validatedPath, depth, request.getDirection(), params);
        cypher.append(traversalPattern);
        
        // WHERE子句
        String whereClause = buildWhereClause(validatedFilters, params);
        if (!whereClause.isEmpty()) {
            cypher.append(" WHERE ").append(whereClause);
        }
        
        // RETURN子句
        cypher.append(buildReturnClause(request.getReturnFormat()));
        
        // 排序和限制
        cypher.append(" ORDER BY length(path)");
        cypher.append(" LIMIT $limit");
        
        log.debug("Generated Cypher: {}", cypher);
        log.debug("Parameters: {}", params);
        
        return new com.ontology.platform.domain.vo.traversal.CypherQuery(cypher.toString(), params);
    }
    
    /**
     * 构建遍历模式
     */
    private String buildTraversalPattern(
            List<TraversalPath> path,
            int depth,
            com.ontology.platform.common.enums.TraversalDirection direction,
            Map<String, Object> params) {
        
        if (path == null || path.isEmpty()) {
            return buildArbitraryTraversalPattern(depth, direction);
        }
        
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            TraversalPath segment = path.get(i);
            pattern.append("-");
            pattern.append(buildRelationPattern(segment.getRelationType(), direction, params, "r" + i));
            pattern.append("->");
            pattern.append("(n").append(i + 1);
            if (segment.getTargetObjectType() != null) {
                pattern.append(":").append(labelize(segment.getTargetObjectType()));
            }
            pattern.append(")");
        }
        
        return pattern.toString();
    }
    
    /**
     * 构建任意关系遍历模式
     */
    private String buildArbitraryTraversalPattern(int depth, 
            com.ontology.platform.common.enums.TraversalDirection direction) {
        String arrow = switch (direction) {
            case OUTGOING -> "->";
            case INCOMING -> "<-";
            case BOTH -> "-";
        };
        
        return String.format("*[1..%d]%s()", depth, arrow);
    }
    
    /**
     * 构建单个关系模式
     */
    private String buildRelationPattern(
            String relationType,
            com.ontology.platform.common.enums.TraversalDirection direction,
            Map<String, Object> params,
            String paramSuffix) {
        
        params.put("relationType_" + paramSuffix, relationType);
        
        StringBuilder pattern = new StringBuilder("[r:");
        pattern.append(labelize(relationType));
        pattern.append(" {relationType: $relationType_").append(paramSuffix).append("}");
        
        return switch (direction) {
            case OUTGOING -> pattern + "]";
            case INCOMING -> pattern + "]";
            case BOTH -> pattern + "*]";
        };
    }
    
    /**
     * 构建WHERE子句
     */
    private String buildWhereClause(List<TraversalFilter> filters, Map<String, Object> params) {
        if (filters == null || filters.isEmpty()) {
            return "";
        }
        
        List<String> clauses = new ArrayList<>();
        int paramIndex = 0;
        
        for (TraversalFilter filter : filters) {
            if (filter.getConditions() == null || filter.getConditions().isEmpty()) {
                continue;
            }
            
            List<String> conditionClauses = new ArrayList<>();
            for (TraversalFilterCondition condition : filter.getConditions()) {
                String clause = buildConditionClause(condition, filter.getDepth(), params, paramIndex++);
                if (clause != null) {
                    conditionClauses.add(clause);
                }
            }
            
            if (!conditionClauses.isEmpty()) {
                String logic = filter.isAndLogic() ? " AND " : " OR ";
                clauses.add("(" + String.join(logic, conditionClauses) + ")");
            }
        }
        
        return String.join(" AND ", clauses);
    }
    
    /**
     * 构建单个条件表达式（完全参数化）
     */
    private String buildConditionClause(
            TraversalFilterCondition condition,
            int depth,
            Map<String, Object> params,
            int paramIndex) {
        
        if (condition == null || condition.getField() == null || condition.getOperator() == null) {
            return null;
        }
        
        String paramName = "p_" + paramIndex + "_" + condition.getField();
        String propertyPath = "n" + depth + "." + condition.getField();
        
        return switch (condition.getOperator()) {
            case eq -> { params.put(paramName, condition.getValue()); yield propertyPath + " = $" + paramName; }
            case ne -> { params.put(paramName, condition.getValue()); yield propertyPath + " <> $" + paramName; }
            case gt -> { params.put(paramName, condition.getValue()); yield propertyPath + " > $" + paramName; }
            case gte -> { params.put(paramName, condition.getValue()); yield propertyPath + " >= $" + paramName; }
            case lt -> { params.put(paramName, condition.getValue()); yield propertyPath + " < $" + paramName; }
            case lte -> { params.put(paramName, condition.getValue()); yield propertyPath + " <= $" + paramName; }
            case isNull -> propertyPath + " IS NULL";
            case isNotNull -> propertyPath + " IS NOT NULL";
            case contains -> { params.put(paramName, "%" + condition.getValue() + "%"); yield propertyPath + " CONTAINS $" + paramName; }
            case startsWith -> { params.put(paramName, condition.getValue() + "%"); yield propertyPath + " STARTS WITH $" + paramName; }
            case endsWith -> { params.put(paramName, "%" + condition.getValue()); yield propertyPath + " ENDS WITH $" + paramName; }
            case in -> { params.put(paramName, condition.getValueList()); yield propertyPath + " IN $" + paramName; }
            case notIn -> { params.put(paramName, condition.getValueList()); yield "NOT (" + propertyPath + " IN $" + paramName + ")"; }
            case between -> {
                List<?> range = condition.getValueList();
                if (range.size() == 2) {
                    params.put(paramName + "_start", range.get(0));
                    params.put(paramName + "_end", range.get(1));
                    yield propertyPath + " >= $" + paramName + "_start AND " + propertyPath + " <= $" + paramName + "_end";
                }
                yield null;
            }
        };
    }
    
    /**
     * 构建RETURN子句
     */
    private String buildReturnClause(com.ontology.platform.common.enums.ReturnFormat format) {
        return switch (format) {
            case GRAPH -> " RETURN path, nodes(path) as nodes, relationships(path) as edges";
            case TREE -> " RETURN path";
            case FLAT -> " RETURN start, collect(nodes(path))[1..-1] as connectedNodes";
        };
    }
    
    /**
     * 将类型名转换为AGE标签格式
     */
    private String labelize(String name) {
        if (name == null || name.isBlank()) {
            return "Object";
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
    
    /**
     * UUID格式校验
     */
    private boolean isValidUUID(String str) {
        if (str == null) {
            return false;
        }
        return str.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }
}
