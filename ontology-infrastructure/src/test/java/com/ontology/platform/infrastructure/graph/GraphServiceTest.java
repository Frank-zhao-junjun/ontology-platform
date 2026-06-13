package com.ontology.platform.infrastructure.graph;

import com.ontology.platform.common.enums.ErrorCode;
import com.ontology.platform.common.enums.RelationCardinality;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.domain.entity.Relation;
import com.ontology.platform.infrastructure.config.GraphProperties;
import com.ontology.platform.infrastructure.service.AgeGraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GraphService 门面委派测试
 * 验证 GraphService 把 createEdge/deleteEdge 正确委派到 AgeGraphService，
 * 并在底层 AGE 抛错时按原样透传。
 */
@DisplayName("GraphService 门面测试")
class GraphServiceTest {

    private AgeGraphService ageGraphService;
    private GraphProperties graphProperties;
    private GraphService graphService;

    @BeforeEach
    void setUp() {
        ageGraphService = Mockito.mock(AgeGraphService.class);
        graphProperties = Mockito.mock(GraphProperties.class);
        when(graphProperties.isDegraded()).thenReturn(false); // 默认非降级模式
        graphService = new GraphService(ageGraphService, graphProperties);
    }

    private Relation buildRelation() {
        return Relation.create(
                "ontology-1",
                "source-type-1",
                "target-type-1",
                "owns",
                "Owns",
                "Ownership relation",
                RelationCardinality.ONE_TO_MANY
        );
    }

    @Test
    @DisplayName("createEdge(Relation) 应委派到 AgeGraphService.createEdge")
    void createEdge_withRelation_delegates() {
        Relation relation = buildRelation();
        doNothing().when(ageGraphService).createEdge(relation);

        graphService.createEdge(relation);

        verify(ageGraphService, times(1)).createEdge(relation);
    }

    @Test
    @DisplayName("deleteEdge(relationId, ontologyId) 应委派到 AgeGraphService.deleteEdge")
    void deleteEdge_withIdAndOntology_delegates() {
        doNothing().when(ageGraphService).deleteEdge("rel-1", "ontology-1");

        graphService.deleteEdge("rel-1", "ontology-1");

        verify(ageGraphService, times(1)).deleteEdge("rel-1", "ontology-1");
    }

    @Test
    @DisplayName("非降级模式下 AGE 抛异常时 createEdge(Relation) 应抛 BusinessException(GRAPH_UNAVAILABLE)")
    void createEdge_throwsBusinessExceptionWhenNotDegraded() {
        Relation relation = buildRelation();
        RuntimeException boom = new RuntimeException("AGE down");
        doThrow(boom).when(ageGraphService).createEdge(relation);

        assertThatThrownBy(() -> graphService.createEdge(relation))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GRAPH_UNAVAILABLE);

        verify(ageGraphService, times(1)).createEdge(relation);
    }

    @Test
    @DisplayName("非降级模式下 AGE 抛异常时 deleteEdge 应抛 BusinessException(GRAPH_UNAVAILABLE)")
    void deleteEdge_throwsBusinessExceptionWhenNotDegraded() {
        RuntimeException boom = new RuntimeException("AGE down");
        doThrow(boom).when(ageGraphService).deleteEdge("rel-1", "ontology-1");

        assertThatThrownBy(() -> graphService.deleteEdge("rel-1", "ontology-1"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GRAPH_UNAVAILABLE);
    }

    @Test
    @DisplayName("降级模式下 AGE 抛异常时 createEdge 静默跳过，不抛异常")
    void createEdge_silentlySkipsWhenDegraded() {
        when(graphProperties.isDegraded()).thenReturn(true);
        Relation relation = buildRelation();
        RuntimeException boom = new RuntimeException("AGE down");
        doThrow(boom).when(ageGraphService).createEdge(relation);

        assertThatCode(() -> graphService.createEdge(relation))
                .doesNotThrowAnyException();

        verify(ageGraphService, times(1)).createEdge(relation);
    }

    @Test
    @DisplayName("降级模式下 AGE 抛异常时 deleteEdge 静默跳过，不抛异常")
    void deleteEdge_silentlySkipsWhenDegraded() {
        when(graphProperties.isDegraded()).thenReturn(true);
        RuntimeException boom = new RuntimeException("AGE down");
        doThrow(boom).when(ageGraphService).deleteEdge("rel-1", "ontology-1");

        assertThatCode(() -> graphService.deleteEdge("rel-1", "ontology-1"))
                .doesNotThrowAnyException();

        verify(ageGraphService, times(1)).deleteEdge("rel-1", "ontology-1");
    }

    @Test
    @DisplayName("向后兼容旧签名 createEdge(String, String, String) 仍可委派")
    void createEdge_legacySignatureStillDelegates() {
        // 即使是旧的三字符串签名，调用也应该成功完成委派
        assertThatCode(() -> graphService.createEdge("src", "tgt", "rel"))
                .doesNotThrowAnyException();

        // 验证委派到了 AgeGraphService（参数被组装成 Relation）
        verify(ageGraphService, times(1)).createEdge(
                Mockito.argThat(rel ->
                        "src".equals(rel.getSourceTypeId())
                                && "tgt".equals(rel.getTargetTypeId())
                                && "rel".equals(rel.getName())));
    }

    @Test
    @DisplayName("向后兼容旧签名 deleteEdge(String, String, String) 仍可委派")
    void deleteEdge_legacySignatureStillDelegates() {
        assertThatCode(() -> graphService.deleteEdge("src", "tgt", "rel"))
                .doesNotThrowAnyException();

        // 旧签名会拼出 "rel:src->tgt" 作为 relationId 近似值，ontologyId 传 null
        verify(ageGraphService, times(1)).deleteEdge("rel:src->tgt", null);
    }

    @Test
    @DisplayName("未调用图操作时不应触发 AgeGraphService")
    void noInvocationByDefault() {
        // 仅初始化，不调用任何图操作
        verify(ageGraphService, never()).createEdge((Relation) Mockito.any());
        verify(ageGraphService, never()).deleteEdge(Mockito.anyString(), Mockito.anyString());
    }
}
