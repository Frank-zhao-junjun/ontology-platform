package com.ontology.platform.infrastructure.service;

import com.ontology.platform.domain.entity.Relation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Apache AGE 图存储服务
 * 用于在图数据库中创建、删除和查询边
 * 
 * 使用 Apache AGE 的 Cypher 语法通过 JdbcTemplate 执行 SQL 语句。
 * 每个本体对应一个独立的图（ontology_<ontologyId>），实现数据隔离。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgeGraphService {

    private static final String GRAPH_NAME = "ontology_graph";

    private final JdbcTemplate jdbcTemplate;

    /**
     * 在图数据库中创建边
     * 
     * @param relation 关系实体
     */
    public void createEdge(Relation relation) {
        log.info("Creating AGE edge for relation: id={}, name={}, source={}, target={}",
                relation.getId(), relation.getName(),
                relation.getSourceTypeId(), relation.getTargetTypeId());

        String graphName = getGraphName(relation.getOntologyId());
        String edgeLabel = sanitizeLabel(relation.getName());

        String cypher = String.format(
                "SELECT * FROM cypher('%s', $$ " +
                "MATCH (a:object_type {id: '%s'}) " +
                "MATCH (b:object_type {id: '%s'}) " +
                "CREATE (a)-[r:%s {id: '%s', name: '%s', displayName: '%s', cardinality: '%s'}]->(b) " +
                "RETURN r " +
                "$$) AS (result agtype)",
                graphName,
                relation.getSourceTypeId(),
                relation.getTargetTypeId(),
                edgeLabel,
                relation.getId(),
                relation.getName(),
                escapeString(relation.getDisplayName()),
                relation.getCardinality() != null ? relation.getCardinality().name() : ""
        );

        try {
            jdbcTemplate.queryForList(cypher);
            log.debug("AGE edge created successfully for relation: {}", relation.getId());
        } catch (Exception e) {
            log.error("Failed to create AGE edge for relation {}: {}", relation.getId(), e.getMessage());
            throw new RuntimeException("Failed to create graph edge", e);
        }
    }

    /**
     * 在图数据库中删除边
     * 
     * @param relationId 关系ID
     * @param ontologyId 本体ID
     */
    public void deleteEdge(String relationId, String ontologyId) {
        log.info("Deleting AGE edge for relation: {}", relationId);

        String graphName = getGraphName(ontologyId);

        String cypher = String.format(
                "SELECT * FROM cypher('%s', $$ " +
                "MATCH ()-[r {id: '%s'}]->() " +
                "DELETE r " +
                "$$) AS (result agtype)",
                graphName,
                relationId
        );

        try {
            jdbcTemplate.queryForList(cypher);
            log.debug("AGE edge deleted successfully for relation: {}", relationId);
        } catch (Exception e) {
            log.error("Failed to delete AGE edge for relation {}: {}", relationId, e.getMessage());
            throw new RuntimeException("Failed to delete graph edge", e);
        }
    }

    /**
     * 更新图数据库中的边属性
     * 
     * @param relation 关系实体
     */
    public void updateEdge(Relation relation) {
        log.info("Updating AGE edge for relation: id={}", relation.getId());

        String graphName = getGraphName(relation.getOntologyId());

        String cypher = String.format(
                "SELECT * FROM cypher('%s', $$ " +
                "MATCH ()-[r {id: '%s'}]->() " +
                "SET r.displayName = '%s', r.description = '%s' " +
                "RETURN r " +
                "$$) AS (result agtype)",
                graphName,
                relation.getId(),
                escapeString(relation.getDisplayName()),
                escapeString(relation.getDescription())
        );

        try {
            jdbcTemplate.queryForList(cypher);
            log.debug("AGE edge updated successfully for relation: {}", relation.getId());
        } catch (Exception e) {
            log.error("Failed to update AGE edge for relation {}: {}", relation.getId(), e.getMessage());
            throw new RuntimeException("Failed to update graph edge", e);
        }
    }

    /**
     * 查询与指定对象类型关联的所有边
     * 
     * @param objectTypeId 对象类型ID
     * @param ontologyId 本体ID
     * @return 关联的关系列表（agtype JSON 格式）
     */
    public List<Map<String, Object>> findEdgesByObjectType(String objectTypeId, String ontologyId) {
        log.debug("Finding AGE edges for objectType: {}", objectTypeId);

        String graphName = getGraphName(ontologyId);

        String cypher = String.format(
                "SELECT * FROM cypher('%s', $$ " +
                "MATCH (n:object_type {id: '%s'})-[r]->(m) " +
                "RETURN r " +
                "$$) AS (result agtype)",
                graphName,
                objectTypeId
        );

        try {
            return jdbcTemplate.queryForList(cypher);
        } catch (Exception e) {
            log.error("Failed to find AGE edges for objectType {}: {}", objectTypeId, e.getMessage());
            return List.of();
        }
    }

    /**
     * 初始化图
     * 在应用启动时调用，确保图存在
     */
    public void initializeGraph() {
        log.info("Initializing AGE graph: {}", GRAPH_NAME);

        try {
            // 加载 AGE 扩展
            jdbcTemplate.execute("LOAD 'age'");

            // 设置搜索路径
            jdbcTemplate.execute("SET search_path = ag_catalog, \"$user\", public");

            // 创建图（如果不存在）
            String checkGraph = String.format(
                    "SELECT count(*) FROM ag_catalog.ag_graph WHERE name = '%s'", GRAPH_NAME);
            Integer count = jdbcTemplate.queryForObject(checkGraph, Integer.class);

            if (count == null || count == 0) {
                String createGraph = String.format(
                        "SELECT * FROM ag_catalog.create_graph('%s')", GRAPH_NAME);
                jdbcTemplate.queryForList(createGraph);
                log.info("AGE graph '{}' created successfully", GRAPH_NAME);
            } else {
                log.info("AGE graph '{}' already exists", GRAPH_NAME);
            }
        } catch (Exception e) {
            log.warn("Failed to initialize AGE graph (Apache AGE may not be available): {}", e.getMessage());
        }
    }

    /**
     * 为本体创建独立的图
     * 
     * @param ontologyId 本体ID
     */
    public void createGraphForOntology(String ontologyId) {
        String graphName = getGraphName(ontologyId);
        log.info("Creating AGE graph for ontology: {}", ontologyId);

        try {
            jdbcTemplate.execute("LOAD 'age'");
            jdbcTemplate.execute("SET search_path = ag_catalog, \"$user\", public");

            String checkGraph = String.format(
                    "SELECT count(*) FROM ag_catalog.ag_graph WHERE name = '%s'", graphName);
            Integer count = jdbcTemplate.queryForObject(checkGraph, Integer.class);

            if (count == null || count == 0) {
                String createGraph = String.format(
                        "SELECT * FROM ag_catalog.create_graph('%s')", graphName);
                jdbcTemplate.queryForList(createGraph);
                log.info("AGE graph '{}' created for ontology {}", graphName, ontologyId);
            }
        } catch (Exception e) {
            log.warn("Failed to create graph for ontology {} (Apache AGE may not be available): {}",
                    ontologyId, e.getMessage());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取本体对应的图名称
     */
    private String getGraphName(String ontologyId) {
        if (ontologyId == null) {
            return GRAPH_NAME;
        }
        return "ontology_" + ontologyId.replace("-", "_");
    }

    /**
     * 清理标签名称，确保符合 Apache AGE 标识符规范
     */
    private String sanitizeLabel(String name) {
        if (name == null || name.isBlank()) {
            return "RELATION";
        }
        // 替换非法字符为下划线，确保以字母开头
        String sanitized = name.replaceAll("[^a-zA-Z0-9_]", "_");
        if (!Character.isLetter(sanitized.charAt(0))) {
            sanitized = "r_" + sanitized;
        }
        return sanitized.toUpperCase();
    }

    /**
     * 转义字符串中的单引号，防止 Cypher 注入
     */
    private String escapeString(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''").replace("\\", "\\\\");
    }
}
