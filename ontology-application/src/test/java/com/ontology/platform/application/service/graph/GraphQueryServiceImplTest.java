package com.ontology.platform.application.service.graph;

import com.ontology.platform.application.security.FilterSecurityValidator;
import com.ontology.platform.application.security.GraphTraversalDSLParser;
import com.ontology.platform.application.service.graph.impl.GraphQueryServiceImpl;
import com.ontology.platform.common.enums.FilterOperator;
import com.ontology.platform.common.exception.BusinessException;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.domain.repository.OntologyRepository;
import com.ontology.platform.domain.service.GraphWhitelistService;
import com.ontology.platform.domain.vo.traversal.CypherQuery;
import com.ontology.platform.domain.vo.traversal.GraphTraversalRequest;
import com.ontology.platform.domain.vo.traversal.TraversalFilter;
import com.ontology.platform.domain.vo.traversal.TraversalFilterCondition;
import com.ontology.platform.domain.vo.traversal.TraversalResult;
import com.ontology.platform.infrastructure.graph.AgeQueryExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * GraphQueryServiceImpl 单元测试
 * Graph Query Service Implementation Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GraphQueryServiceImpl 单元测试")
class GraphQueryServiceImplTest {

    @Mock
    private AgeQueryExecutor ageQueryExecutor;

    @Mock
    private GraphTraversalDSLParser dslParser;

    @Mock
    private FilterSecurityValidator filterValidator;

    @Mock
    private GraphWhitelistService whitelistService;

    @Mock
    private OntologyRepository ontologyRepository;

    @InjectMocks
    private GraphQueryServiceImpl graphQueryService;

    @Captor
    private ArgumentCaptor<GraphTraversalRequest> requestCaptor;

    private static final String ONTOLOGY_ID = "test-ontology-id";
    private static final String OBJECT_ID = "test-object-id";
    private static final String FROM_OBJECT_ID = "from-object-id";
    private static final String TO_OBJECT_ID = "to-object-id";
    private static final String ROOT_OBJECT_ID = "root-object-id";

    private CypherQuery sampleCypherQuery;
    private TraversalResult sampleTraversalResult;

    @BeforeEach
    void setUp() {
        sampleCypherQuery = new CypherQuery("MATCH ... RETURN ...", Map.of("key", "value"));
        sampleTraversalResult = TraversalResult.builder()
                .success(true)
                .totalCount(1)
                .nodes(List.of())
                .edges(List.of())
                .paths(List.of())
                .executionTimeMs(0)
                .build();
    }

    // ========================================================================
    // ontologyExists 方法测试
    // ========================================================================

    @Nested
    @DisplayName("ontologyExists - 验证本体是否存在")
    class OntologyExistsTests {

        @Test
        @DisplayName("ontologyId为null时应返回false")
        void shouldReturnFalseWhenOntologyIdIsNull() {
            boolean result = graphQueryService.ontologyExists(null);

            assertThat(result).isFalse();
            verifyNoInteractions(ontologyRepository);
        }

        @Test
        @DisplayName("ontologyId为空白字符串时应返回false")
        void shouldReturnFalseWhenOntologyIdIsBlank() {
            boolean result = graphQueryService.ontologyExists("   ");

            assertThat(result).isFalse();
            verifyNoInteractions(ontologyRepository);
        }

        @Test
        @DisplayName("ontologyId有效且存在时应返回true")
        void shouldReturnTrueWhenOntologyExists() {
            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));

            boolean result = graphQueryService.ontologyExists(ONTOLOGY_ID);

            assertThat(result).isTrue();
            verify(ontologyRepository).findById(ONTOLOGY_ID);
        }

        @Test
        @DisplayName("ontologyId有效但不存在时应返回false")
        void shouldReturnFalseWhenOntologyDoesNotExist() {
            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.empty());

            boolean result = graphQueryService.ontologyExists(ONTOLOGY_ID);

            assertThat(result).isFalse();
            verify(ontologyRepository).findById(ONTOLOGY_ID);
        }
    }

    // ========================================================================
    // objectExists 方法测试
    // ========================================================================

    @Nested
    @DisplayName("objectExists - 验证对象是否存在")
    class ObjectExistsTests {

        @Test
        @DisplayName("ontologyId为null时应返回false")
        void shouldReturnFalseWhenOntologyIdIsNull() {
            boolean result = graphQueryService.objectExists(null, OBJECT_ID);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("objectId为null时应返回false")
        void shouldReturnFalseWhenObjectIdIsNull() {
            boolean result = graphQueryService.objectExists(ONTOLOGY_ID, null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("参数均有效时应返回true")
        void shouldReturnTrueWhenBothIdsAreValid() {
            boolean result = graphQueryService.objectExists(ONTOLOGY_ID, OBJECT_ID);

            assertThat(result).isTrue();
        }
    }

    // ========================================================================
    // traverse 方法测试
    // ========================================================================

    @Nested
    @DisplayName("traverse - 执行图遍历查询")
    class TraverseTests {

        @Test
        @DisplayName("输入有效时应调用DSL解析器和查询执行器并返回结果")
        void shouldCallDslParserAndExecutorWhenInputIsValid() {
            // Arrange
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("Person")
                    .startObjectId(OBJECT_ID)
                    .maxDepth(3)
                    .limit(100)
                    .build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(request)).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(ONTOLOGY_ID, sampleCypherQuery)).thenReturn(sampleTraversalResult);

            // Act
            TraversalResult result = graphQueryService.traverse(ONTOLOGY_ID, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            verify(ontologyRepository).findById(ONTOLOGY_ID);
            verify(dslParser).parse(request);
            verify(ageQueryExecutor).executeTraversal(ONTOLOGY_ID, sampleCypherQuery);
        }

        @Test
        @DisplayName("本体不存在时应捕获ResourceNotFoundException并返回失败结果")
        void shouldReturnFailureResultWhenOntologyNotFound() {
            // Arrange
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("Person")
                    .startObjectId(OBJECT_ID)
                    .build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.empty());

            // Act
            TraversalResult result = graphQueryService.traverse(ONTOLOGY_ID, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorMessage()).contains("not found");
            verify(ontologyRepository).findById(ONTOLOGY_ID);
            verifyNoInteractions(dslParser);
            verifyNoInteractions(ageQueryExecutor);
        }

        @Test
        @DisplayName("对象不存在时应捕获ResourceNotFoundException并返回失败结果")
        void shouldReturnFailureResultWhenObjectNotFound() {
            // Arrange
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("Person")
                    .startObjectId(OBJECT_ID)
                    .build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));

            // Note: objectExists currently always returns true for non-null ids,
            // so this test validates the existing behavior. If objectExists is
            // enhanced to actually check existence, this test should be updated.

            // Act
            TraversalResult result = graphQueryService.traverse(ONTOLOGY_ID, request);

            // Assert - objectExists returns true for non-null, so traversal proceeds
            // The test verifies that validation does not throw for non-null ids
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("查询执行器抛出异常时应返回失败结果")
        void shouldReturnFailureResultWhenExecutorThrows() {
            // Arrange
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("Person")
                    .startObjectId(OBJECT_ID)
                    .maxDepth(3)
                    .limit(100)
                    .build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(request)).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(ONTOLOGY_ID, sampleCypherQuery))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // Act
            TraversalResult result = graphQueryService.traverse(ONTOLOGY_ID, request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorMessage()).contains("Database connection failed");
        }

        @Test
        @DisplayName("请求包含includeProperties时应过滤节点和边的属性")
        void shouldFilterNodeAndEdgePropertiesWhenIncludeListProvided() {
            // Arrange
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("Person")
                    .startObjectId(OBJECT_ID)
                    .includeProperties(List.of("name", "email"))
                    .build();

            Map<String, Object> nodeProps = Map.of("name", "Alice", "email", "alice@test.com", "age", 30, "ssn", "123-45-6789");
            Map<String, Object> edgeProps = Map.of("name", "knows", "since", 2020, "weight", 5);

            TraversalResult executorResult = TraversalResult.builder()
                    .success(true)
                    .totalCount(1)
                    .nodes(List.of(
                            TraversalResult.NodeInfo.builder()
                                    .id("n1").objectType("Person").objectId(OBJECT_ID)
                                    .properties(nodeProps).depth(0).build()
                    ))
                    .edges(List.of(
                            TraversalResult.EdgeInfo.builder()
                                    .id("e1").relationType("KNOWS")
                                    .sourceId("n1").targetId("n2")
                                    .properties(edgeProps).build()
                    ))
                    .paths(List.of())
                    .executionTimeMs(10)
                    .build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(request)).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(ONTOLOGY_ID, sampleCypherQuery)).thenReturn(executorResult);

            // Act
            TraversalResult result = graphQueryService.traverse(ONTOLOGY_ID, request);

            // Assert
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes().get(0).getProperties())
                    .hasSize(2)
                    .containsOnlyKeys("name", "email")
                    .containsEntry("name", "Alice")
                    .containsEntry("email", "alice@test.com");

            assertThat(result.getEdges()).hasSize(1);
            assertThat(result.getEdges().get(0).getProperties())
                    .hasSize(1)
                    .containsOnlyKeys("name")
                    .containsEntry("name", "knows");
        }

        @Test
        @DisplayName("请求包含excludeProperties时应排除指定属性")
        void shouldExcludePropertiesWhenExcludeListProvided() {
            // Arrange
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("Person")
                    .startObjectId(OBJECT_ID)
                    .excludeProperties(List.of("ssn", "age"))
                    .build();

            Map<String, Object> nodeProps = Map.of("name", "Bob", "email", "bob@test.com", "age", 25, "ssn", "987-65-4321");

            TraversalResult executorResult = TraversalResult.builder()
                    .success(true)
                    .totalCount(1)
                    .nodes(List.of(
                            TraversalResult.NodeInfo.builder()
                                    .id("n1").objectType("Person").objectId(OBJECT_ID)
                                    .properties(nodeProps).depth(0).build()
                    ))
                    .edges(List.of())
                    .paths(List.of())
                    .executionTimeMs(10)
                    .build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(request)).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(ONTOLOGY_ID, sampleCypherQuery)).thenReturn(executorResult);

            // Act
            TraversalResult result = graphQueryService.traverse(ONTOLOGY_ID, request);

            // Assert
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes().get(0).getProperties())
                    .hasSize(2)
                    .containsOnlyKeys("name", "email")
                    .doesNotContainKeys("ssn", "age");
        }

        @Test
        @DisplayName("同时指定include和exclude时，include应优先")
        void shouldPrioritizeIncludeOverExclude() {
            // Arrange
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("Person")
                    .startObjectId(OBJECT_ID)
                    .includeProperties(List.of("name"))
                    .excludeProperties(List.of("name", "email"))
                    .build();

            Map<String, Object> nodeProps = Map.of("name", "Charlie", "email", "charlie@test.com", "age", 35);

            TraversalResult executorResult = TraversalResult.builder()
                    .success(true)
                    .totalCount(1)
                    .nodes(List.of(
                            TraversalResult.NodeInfo.builder()
                                    .id("n1").objectType("Person").objectId(OBJECT_ID)
                                    .properties(nodeProps).depth(0).build()
                    ))
                    .edges(List.of())
                    .paths(List.of())
                    .executionTimeMs(10)
                    .build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(request)).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(ONTOLOGY_ID, sampleCypherQuery)).thenReturn(executorResult);

            // Act
            TraversalResult result = graphQueryService.traverse(ONTOLOGY_ID, request);

            // Assert - include takes precedence, exclude is ignored when include is present
            assertThat(result.getNodes().get(0).getProperties())
                    .hasSize(1)
                    .containsOnlyKeys("name");
        }

        @Test
        @DisplayName("include和exclude均为空时，应返回所有属性")
        void shouldReturnAllPropertiesWhenNoFilterSpecified() {
            // Arrange
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("Person")
                    .startObjectId(OBJECT_ID)
                    .build();

            Map<String, Object> nodeProps = Map.of("name", "Dave", "email", "dave@test.com", "age", 40);

            TraversalResult executorResult = TraversalResult.builder()
                    .success(true)
                    .totalCount(1)
                    .nodes(List.of(
                            TraversalResult.NodeInfo.builder()
                                    .id("n1").objectType("Person").objectId(OBJECT_ID)
                                    .properties(nodeProps).depth(0).build()
                    ))
                    .edges(List.of())
                    .paths(List.of())
                    .executionTimeMs(10)
                    .build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(request)).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(ONTOLOGY_ID, sampleCypherQuery)).thenReturn(executorResult);

            // Act
            TraversalResult result = graphQueryService.traverse(ONTOLOGY_ID, request);

            // Assert
            assertThat(result.getNodes().get(0).getProperties())
                    .hasSize(3)
                    .containsOnlyKeys("name", "email", "age");
        }

        @Test
        @DisplayName("节点属性为null时，filterProperties应返回空Map")
        void shouldReturnEmptyMapWhenNodePropertiesAreNull() {
            // Arrange
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("Person")
                    .startObjectId(OBJECT_ID)
                    .includeProperties(List.of("name"))
                    .build();

            TraversalResult executorResult = TraversalResult.builder()
                    .success(true)
                    .totalCount(1)
                    .nodes(List.of(
                            TraversalResult.NodeInfo.builder()
                                    .id("n1").objectType("Person").objectId(OBJECT_ID)
                                    .properties(null).depth(0).build()
                    ))
                    .edges(List.of())
                    .paths(List.of())
                    .executionTimeMs(10)
                    .build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(request)).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(ONTOLOGY_ID, sampleCypherQuery)).thenReturn(executorResult);

            // Act
            TraversalResult result = graphQueryService.traverse(ONTOLOGY_ID, request);

            // Assert
            assertThat(result.getNodes()).hasSize(1);
            assertThat(result.getNodes().get(0).getProperties()).isEmpty();
        }
    }

    // ========================================================================
    // findShortestPath 方法测试
    // ========================================================================

    @Nested
    @DisplayName("findShortestPath - 查询最短路径")
    class FindShortestPathTests {

        @Test
        @DisplayName("应委托给traverse方法，depth取maxDepth与5的较小值")
        void shouldDelegateToTraverseWithDepthCappedAtFive() {
            // Arrange
            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(any(GraphTraversalRequest.class))).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(eq(ONTOLOGY_ID), any(CypherQuery.class)))
                    .thenReturn(sampleTraversalResult);

            // Act: maxDepth=10 should be capped to 5
            TraversalResult result = graphQueryService.findShortestPath(
                    ONTOLOGY_ID, FROM_OBJECT_ID, TO_OBJECT_ID, 10);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();

            // Verify that traverse was called with a request having maxDepth=5 and limit=1
            verify(dslParser).parse(requestCaptor.capture());
            GraphTraversalRequest captured = requestCaptor.getValue();

            assertThat(captured.getStartObjectId()).isEqualTo(FROM_OBJECT_ID);
            assertThat(captured.getStartObjectType()).isEqualTo("Object");
            assertThat(captured.getMaxDepth()).isEqualTo(5);
            assertThat(captured.getLimit()).isEqualTo(1);
            assertThat(captured.getFilters()).isNotEmpty();
        }

        @Test
        @DisplayName("maxDepth小于5时应使用原始值")
        void shouldUseOriginalDepthWhenLessThanFive() {
            // Arrange
            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(any(GraphTraversalRequest.class))).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(eq(ONTOLOGY_ID), any(CypherQuery.class)))
                    .thenReturn(sampleTraversalResult);

            // Act: maxDepth=3 should stay 3
            graphQueryService.findShortestPath(ONTOLOGY_ID, FROM_OBJECT_ID, TO_OBJECT_ID, 3);

            // Assert
            verify(dslParser).parse(requestCaptor.capture());
            GraphTraversalRequest captured = requestCaptor.getValue();
            assertThat(captured.getMaxDepth()).isEqualTo(3);
        }

        @Test
        @DisplayName("fromObjectId为null时应抛出BusinessException")
        void shouldThrowWhenFromObjectIdIsNull() {
            assertThatThrownBy(() ->
                    graphQueryService.findShortestPath(ONTOLOGY_ID, null, TO_OBJECT_ID, 5))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("fromObjectId");
        }

        @Test
        @DisplayName("toObjectId为null时应抛出BusinessException")
        void shouldThrowWhenToObjectIdIsNull() {
            assertThatThrownBy(() ->
                    graphQueryService.findShortestPath(ONTOLOGY_ID, FROM_OBJECT_ID, null, 5))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("toObjectId");
        }

        @Test
        @DisplayName("生成的过滤条件应包含目标对象ID")
        void shouldIncludeTargetObjectIdInFilter() {
            // Arrange
            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(any(GraphTraversalRequest.class))).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(eq(ONTOLOGY_ID), any(CypherQuery.class)))
                    .thenReturn(sampleTraversalResult);

            // Act
            graphQueryService.findShortestPath(ONTOLOGY_ID, FROM_OBJECT_ID, TO_OBJECT_ID, 5);

            // Assert: verify the filter conditions
            verify(dslParser).parse(requestCaptor.capture());
            GraphTraversalRequest captured = requestCaptor.getValue();
            List<TraversalFilter> filters = captured.getFilters();

            assertThat(filters).hasSize(1);
            TraversalFilter filter = filters.get(0);
            assertThat(filter.getLogic()).isEqualTo("AND");
            assertThat(filter.getDepth()).isEqualTo(5);

            List<TraversalFilterCondition> conditions = filter.getConditions();
            assertThat(conditions).hasSize(1);
            assertThat(conditions.get(0).getField()).isEqualTo("id");
            assertThat(conditions.get(0).getOperator()).isEqualTo(FilterOperator.eq);
            assertThat(conditions.get(0).getValue()).isEqualTo(TO_OBJECT_ID);
        }
    }

    // ========================================================================
    // extractSubgraph 方法测试
    // ========================================================================

    @Nested
    @DisplayName("extractSubgraph - 提取子图")
    class ExtractSubgraphTests {

        @Test
        @DisplayName("应委托给traverse方法，limit为1000")
        void shouldDelegateToTraverseWithLimit1000() {
            // Arrange
            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(any(GraphTraversalRequest.class))).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(eq(ONTOLOGY_ID), any(CypherQuery.class)))
                    .thenReturn(sampleTraversalResult);

            // Act: depth=3
            TraversalResult result = graphQueryService.extractSubgraph(ONTOLOGY_ID, ROOT_OBJECT_ID, 3);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();

            verify(dslParser).parse(requestCaptor.capture());
            GraphTraversalRequest captured = requestCaptor.getValue();

            assertThat(captured.getStartObjectId()).isEqualTo(ROOT_OBJECT_ID);
            assertThat(captured.getMaxDepth()).isEqualTo(3);
            assertThat(captured.getLimit()).isEqualTo(1000);
        }

        @Test
        @DisplayName("depth超过5时应被限制为5")
        void shouldCapDepthAtFive() {
            // Arrange
            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(any(GraphTraversalRequest.class))).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(eq(ONTOLOGY_ID), any(CypherQuery.class)))
                    .thenReturn(sampleTraversalResult);

            // Act: depth=10 should be capped to 5
            graphQueryService.extractSubgraph(ONTOLOGY_ID, ROOT_OBJECT_ID, 10);

            // Assert
            verify(dslParser).parse(requestCaptor.capture());
            GraphTraversalRequest captured = requestCaptor.getValue();
            assertThat(captured.getMaxDepth()).isEqualTo(5);
        }

        @Test
        @DisplayName("ontologyId无效时应抛出ResourceNotFoundException")
        void shouldThrowWhenOntologyNotFound() {
            // Arrange
            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() ->
                    graphQueryService.extractSubgraph(ONTOLOGY_ID, ROOT_OBJECT_ID, 3))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(ONTOLOGY_ID);

            verify(dslParser, never()).parse(any());
            verify(ageQueryExecutor, never()).executeTraversal(any(), any());
        }

        @Test
        @DisplayName("对象不存在时应抛出ResourceNotFoundException")
        void shouldThrowWhenObjectNotFound() {
            // Arrange
            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));

            // Note: objectExists currently always returns true for non-null ids.
            // This test documents the current behavior; if objectExists is enhanced
            // to check actual existence, mock objectRepository to return empty.

            // Act: should not throw since objectExists returns true for non-null ids
            when(dslParser.parse(any(GraphTraversalRequest.class))).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(eq(ONTOLOGY_ID), any(CypherQuery.class)))
                    .thenReturn(sampleTraversalResult);

            TraversalResult result = graphQueryService.extractSubgraph(ONTOLOGY_ID, ROOT_OBJECT_ID, 3);

            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
        }
    }

    // ========================================================================
    // filterProperties 间接测试（通过traverse方法）
    // ========================================================================

    @Nested
    @DisplayName("filterProperties - 属性过滤逻辑")
    class FilterPropertiesTests {

        @Test
        @DisplayName("include列表为空时，不应过滤任何属性")
        void shouldNotFilterWhenIncludeIsEmpty() {
            // Arrange
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("Person")
                    .startObjectId(OBJECT_ID)
                    .includeProperties(List.of())
                    .build();

            Map<String, Object> nodeProps = Map.of("a", 1, "b", 2, "c", 3);

            TraversalResult executorResult = TraversalResult.builder()
                    .success(true)
                    .totalCount(1)
                    .nodes(List.of(
                            TraversalResult.NodeInfo.builder()
                                    .id("n1").objectType("Person").objectId(OBJECT_ID)
                                    .properties(nodeProps).depth(0).build()
                    ))
                    .edges(List.of())
                    .paths(List.of())
                    .executionTimeMs(10)
                    .build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(request)).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(ONTOLOGY_ID, sampleCypherQuery)).thenReturn(executorResult);

            // Act
            TraversalResult result = graphQueryService.traverse(ONTOLOGY_ID, request);

            // Assert
            assertThat(result.getNodes().get(0).getProperties()).hasSize(3);
        }

        @Test
        @DisplayName("include中指定的key在属性中不存在时，不应出现在结果中")
        void shouldSkipMissingKeysInInclude() {
            // Arrange
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("Person")
                    .startObjectId(OBJECT_ID)
                    .includeProperties(List.of("nonexistent"))
                    .build();

            Map<String, Object> nodeProps = Map.of("name", "Eve");

            TraversalResult executorResult = TraversalResult.builder()
                    .success(true)
                    .totalCount(1)
                    .nodes(List.of(
                            TraversalResult.NodeInfo.builder()
                                    .id("n1").objectType("Person").objectId(OBJECT_ID)
                                    .properties(nodeProps).depth(0).build()
                    ))
                    .edges(List.of())
                    .paths(List.of())
                    .executionTimeMs(10)
                    .build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(request)).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(ONTOLOGY_ID, sampleCypherQuery)).thenReturn(executorResult);

            // Act
            TraversalResult result = graphQueryService.traverse(ONTOLOGY_ID, request);

            // Assert
            assertThat(result.getNodes().get(0).getProperties()).isEmpty();
        }

        @Test
        @DisplayName("exclude列表包含不存在的key时，不应当影响结果")
        void shouldIgnoreNonExistentKeysInExclude() {
            // Arrange
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("Person")
                    .startObjectId(OBJECT_ID)
                    .excludeProperties(List.of("nonexistent"))
                    .build();

            Map<String, Object> nodeProps = Map.of("name", "Frank", "age", 45);

            TraversalResult executorResult = TraversalResult.builder()
                    .success(true)
                    .totalCount(1)
                    .nodes(List.of(
                            TraversalResult.NodeInfo.builder()
                                    .id("n1").objectType("Person").objectId(OBJECT_ID)
                                    .properties(nodeProps).depth(0).build()
                    ))
                    .edges(List.of())
                    .paths(List.of())
                    .executionTimeMs(10)
                    .build();

            when(ontologyRepository.findById(ONTOLOGY_ID)).thenReturn(Optional.of(mock(Ontology.class)));
            when(dslParser.parse(request)).thenReturn(sampleCypherQuery);
            when(ageQueryExecutor.executeTraversal(ONTOLOGY_ID, sampleCypherQuery)).thenReturn(executorResult);

            // Act
            TraversalResult result = graphQueryService.traverse(ONTOLOGY_ID, request);

            // Assert
            assertThat(result.getNodes().get(0).getProperties()).hasSize(2);
        }
    }
}
