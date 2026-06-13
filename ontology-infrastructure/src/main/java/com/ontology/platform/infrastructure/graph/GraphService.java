package com.ontology.platform.infrastructure.graph;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.infrastructure.config.GraphProperties;
import com.ontology.platform.infrastructure.service.AgeGraphService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 图服务门面
 * 用于在图数据库中创建/删除边的统一入口
 *
 * 委派到 {@link AgeGraphService} 执行实际 Apache AGE 操作。
 * 保留旧字符串签名以兼容历史调用方。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GraphService {

    private final AgeGraphService ageGraphService;
    private final GraphProperties graphProperties;

    /**
     * 创建图边（新签名，接收完整关系实体）
     *
     * @param relation 关系实体
     */
    public void createEdge(Relation relation) {
        log.debug("委派 createEdge 到 AgeGraphService: relationId={}, name={}",
                relation.getId(), relation.getName());
        try {
            ageGraphService.createEdge(relation);
        } catch (Exception e) {
            if (graphProperties.isDegraded()) {
                log.warn("AGE 不可用 (降级模式)，跳过 createEdge: relationId={}, error={}",
                        relation.getId(), e.getMessage());
            } else {
                throw new BusinessException(ErrorCode.GRAPH_UNAVAILABLE,
                        "Failed to create graph edge: " + e.getMessage());
            }
        }
    }

    /**
     * 创建图边（向后兼容的旧签名）
     *
     * <p>仅提供 sourceId/targetId/relationName 时，会构造一个最小关系的占位并尝试同步。
     * 缺少 ontologyId 与 关系主键时，底层 Cypher 将无法精确定位目标本体图与边，
     * 推荐新代码改用 {@link #createEdge(Relation)}。</p>
     *
     * @param sourceId     源对象类型ID
     * @param targetId     目标对象类型ID
     * @param relationName 关系名称
     */
    @Deprecated
    public void createEdge(String sourceId, String targetId, String relationName) {
        log.warn("调用已废弃的 createEdge(String, String, String)，建议改用 createEdge(Relation): sourceId={}, targetId={}, name={}",
                sourceId, targetId, relationName);
        Relation stub = Relation.builder()
                .id(relationName)
                .sourceTypeId(sourceId)
                .targetTypeId(targetId)
                .name(relationName)
                .displayName(relationName)
                .build();
        ageGraphService.createEdge(stub);
    }

    /**
     * 删除图边（新签名，接收关系ID和本体ID）
     *
     * @param relationId 关系ID
     * @param ontologyId 本体ID
     */
    public void deleteEdge(String relationId, String ontologyId) {
        log.debug("委派 deleteEdge 到 AgeGraphService: relationId={}, ontologyId={}",
                relationId, ontologyId);
        try {
            ageGraphService.deleteEdge(relationId, ontologyId);
        } catch (Exception e) {
            if (graphProperties.isDegraded()) {
                log.warn("AGE 不可用 (降级模式)，跳过 deleteEdge: relationId={}, error={}",
                        relationId, e.getMessage());
            } else {
                throw new BusinessException(ErrorCode.GRAPH_UNAVAILABLE,
                        "Failed to delete graph edge: " + e.getMessage());
            }
        }
    }

    /**
     * 删除图边（向后兼容的旧签名）
     *
     * <p>仅提供 sourceId/targetId/relationName 时，使用关系名作为边ID的近似值，
     * 缺少 ontologyId 会让底层无法确定目标图。推荐新代码改用
     * {@link #deleteEdge(String, String)}。</p>
     *
     * @param sourceId     源对象类型ID
     * @param targetId     目标对象类型ID
     * @param relationName 关系名称
     */
    @Deprecated
    public void deleteEdge(String sourceId, String targetId, String relationName) {
        log.warn("调用已废弃的 deleteEdge(String, String, String)，建议改用 deleteEdge(String, String): sourceId={}, targetId={}, name={}",
                sourceId, targetId, relationName);
        ageGraphService.deleteEdge(relationIdFromLegacy(relationName, sourceId, targetId), null);
    }

    /**
     * 在没有关系ID的情况下从旧字段拼接一个稳定的关系ID近似值。
     */
    private String relationIdFromLegacy(String relationName, String sourceId, String targetId) {
        return String.valueOf(relationName) + ":" + sourceId + "->" + targetId;
    }
}
