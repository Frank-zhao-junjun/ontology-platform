package com.ontology.platform.infrastructure.service;

import com.ontology.platform.common.enums.RelationCardinality;
import com.ontology.platform.domain.entity.Relation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AgeGraphService 单元测试
 * 覆盖所有公共方法及 Cypher 生成正确性、标签清理、字符串转义、图名构造等逻辑
 */
@DisplayName("AgeGraphService 单元测试")
class AgeGraphServiceTest {

    private JdbcTemplate jdbcTemplate;
    private AgeGraphService ageGraphService;

    @BeforeEach
    void setUp() {
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        ageGraphService = new AgeGraphService(jdbcTemplate);
    }

    private Relation createTestRelation() {
        return Relation.builder()
                .id("rel-001")
                .ontologyId("ontology-1")
                .sourceTypeId("source-type-1")
                .targetTypeId("target-type-1")
                .name("owns")
                .displayName("Owns")
                .description("Ownership relation")
                .cardinality(RelationCardinality.ONE_TO_MANY)
                .build();
    }

    // ==================== createEdge ====================

    @Nested
    @DisplayName("createEdge 方法")
    class CreateEdgeTests {

        @Test
        @DisplayName("应生成正确的 Cypher 并调用 JdbcTemplate.queryForList")
        void createEdge_shouldGenerateCorrectCypher() {
            Relation relation = createTestRelation();

            ageGraphService.createEdge(relation);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate, times(1)).queryForList(sqlCaptor.capture());

            String sql = sqlCaptor.getValue();
            assertThat(sql)
                    .contains("SELECT * FROM cypher('ontology_ontology_1'")
                    .contains("MATCH (a:object_type {id: 'source-type-1'})")
                    .contains("MATCH (b:object_type {id: 'target-type-1'})")
                    .contains("CREATE (a)-[r:OWNS {id: 'rel-001', name: 'owns', displayName: 'Owns', cardinality: 'ONE_TO_MANY'}]->(b)")
                    .contains("RETURN r")
                    .contains("$$) AS (result agtype)");
        }

        @Test
        @DisplayName("关系名称为空时应使用默认标签 RELATION")
        void createEdge_shouldUseDefaultLabel_whenNameIsBlank() {
            Relation relation = createTestRelation();
            relation.setName("");

            ageGraphService.createEdge(relation);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue()).contains("-[r:RELATION {");
        }

        @Test
        @DisplayName("关系名称为 null 时应使用默认标签 RELATION")
        void createEdge_shouldUseDefaultLabel_whenNameIsNull() {
            Relation relation = createTestRelation();
            relation.setName(null);

            ageGraphService.createEdge(relation);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue()).contains("-[r:RELATION {");
        }

        @Test
        @DisplayName("关系名称含特殊字符时应执行 sanitizeLabel（替换非法字符、转大写）")
        void createEdge_shouldSanitizeLabel_withSpecialChars() {
            Relation relation = createTestRelation();
            relation.setName("has-parent");

            ageGraphService.createEdge(relation);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue()).contains("-[r:HAS_PARENT {");
        }

        @Test
        @DisplayName("关系名称以数字开头时应添加 r_ 前缀")
        void createEdge_shouldAddPrefix_whenNameStartsWithDigit() {
            Relation relation = createTestRelation();
            relation.setName("123relation");

            ageGraphService.createEdge(relation);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue()).contains("-[r:R_123RELATION {");
        }

        @Test
        @DisplayName("ontologyId 为空时应使用默认图名 ontology_graph")
        void createEdge_shouldUseDefaultGraphName_whenOntologyIdIsNull() {
            Relation relation = createTestRelation();
            relation.setOntologyId(null);

            ageGraphService.createEdge(relation);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue()).contains("cypher('ontology_graph'");
        }

        @Test
        @DisplayName("displayName 中的单引号和反斜杠应被正确转义")
        void createEdge_shouldEscapeSpecialCharactersInDisplayName() {
            Relation relation = createTestRelation();
            relation.setDisplayName("It's a \"test\" with back\\slash");

            ageGraphService.createEdge(relation);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue())
                    .contains("displayName: 'It''s a \"test\" with back\\\\slash'");
        }

        @Test
        @DisplayName("cardinality 为 null 时应使用空字符串")
        void createEdge_shouldHandleNullCardinality() {
            Relation relation = createTestRelation();
            relation.setCardinality(null);

            ageGraphService.createEdge(relation);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue()).contains("cardinality: ''");
        }

        @Test
        @DisplayName("JdbcTemplate 抛出异常时应包装为 RuntimeException")
        void createEdge_shouldThrowRuntimeException_whenJdbcTemplateFails() {
            Relation relation = createTestRelation();
            doThrow(new RuntimeException("DB connection lost"))
                    .when(jdbcTemplate).queryForList(anyString());

            assertThatThrownBy(() -> ageGraphService.createEdge(relation))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Failed to create graph edge")
                    .hasCauseInstanceOf(RuntimeException.class);
        }
    }

    // ==================== deleteEdge ====================

    @Nested
    @DisplayName("deleteEdge 方法")
    class DeleteEdgeTests {

        @Test
        @DisplayName("应生成正确的 Cypher 并调用 JdbcTemplate.queryForList")
        void deleteEdge_shouldGenerateCorrectCypher() {
            ageGraphService.deleteEdge("rel-001", "ontology-1");

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());

            String sql = sqlCaptor.getValue();
            assertThat(sql)
                    .contains("SELECT * FROM cypher('ontology_ontology_1'")
                    .contains("MATCH ()-[r {id: 'rel-001'}]->()")
                    .contains("DELETE r")
                    .contains("$$) AS (result agtype)");
        }

        @Test
        @DisplayName("ontologyId 为空时应使用默认图名 ontology_graph")
        void deleteEdge_shouldUseDefaultGraphName_whenOntologyIdIsNull() {
            ageGraphService.deleteEdge("rel-001", null);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue()).contains("cypher('ontology_graph'");
        }

        @Test
        @DisplayName("JdbcTemplate 抛出异常时应包装为 RuntimeException")
        void deleteEdge_shouldThrowRuntimeException_whenJdbcTemplateFails() {
            doThrow(new RuntimeException("DB error"))
                    .when(jdbcTemplate).queryForList(anyString());

            assertThatThrownBy(() -> ageGraphService.deleteEdge("rel-001", "ontology-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Failed to delete graph edge")
                    .hasCauseInstanceOf(RuntimeException.class);
        }
    }

    // ==================== updateEdge ====================

    @Nested
    @DisplayName("updateEdge 方法")
    class UpdateEdgeTests {

        @Test
        @DisplayName("应生成正确的 Cypher 并调用 JdbcTemplate.queryForList")
        void updateEdge_shouldGenerateCorrectCypher() {
            Relation relation = createTestRelation();

            ageGraphService.updateEdge(relation);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());

            String sql = sqlCaptor.getValue();
            assertThat(sql)
                    .contains("SELECT * FROM cypher('ontology_ontology_1'")
                    .contains("MATCH ()-[r {id: 'rel-001'}]->()")
                    .contains("SET r.displayName = 'Owns', r.description = 'Ownership relation'")
                    .contains("RETURN r")
                    .contains("$$) AS (result agtype)");
        }

        @Test
        @DisplayName("description 中的特殊字符应被正确转义")
        void updateEdge_shouldEscapeDescription() {
            Relation relation = createTestRelation();
            relation.setDescription("It's a description with back\\slash");

            ageGraphService.updateEdge(relation);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue())
                    .contains("r.description = 'It''s a description with back\\\\slash'");
        }

        @Test
        @DisplayName("description 为 null 时应使用空字符串")
        void updateEdge_shouldHandleNullDescription() {
            Relation relation = createTestRelation();
            relation.setDescription(null);

            ageGraphService.updateEdge(relation);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue()).contains("r.description = ''");
        }

        @Test
        @DisplayName("ontologyId 为空时应使用默认图名")
        void updateEdge_shouldUseDefaultGraphName_whenOntologyIdIsNull() {
            Relation relation = createTestRelation();
            relation.setOntologyId(null);

            ageGraphService.updateEdge(relation);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue()).contains("cypher('ontology_graph'");
        }
    }

    // ==================== findEdgesByObjectType ====================

    @Nested
    @DisplayName("findEdgesByObjectType 方法")
    class FindEdgesByObjectTypeTests {

        @Test
        @DisplayName("应生成正确的 Cypher 并返回 JdbcTemplate 的查询结果")
        void findEdgesByObjectType_shouldReturnResults() {
            List<Map<String, Object>> expected = List.of(Map.of("result", "agtype_value"));
            when(jdbcTemplate.queryForList(anyString())).thenReturn(expected);

            List<Map<String, Object>> result =
                    ageGraphService.findEdgesByObjectType("source-type-1", "ontology-1");

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());

            String sql = sqlCaptor.getValue();
            assertThat(sql)
                    .contains("SELECT * FROM cypher('ontology_ontology_1'")
                    .contains("MATCH (n:object_type {id: 'source-type-1'})-[r]->(m)")
                    .contains("RETURN r")
                    .contains("$$) AS (result agtype)");
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("无匹配结果时应返回空列表")
        void findEdgesByObjectType_shouldReturnEmptyList_whenNoResults() {
            when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of());

            List<Map<String, Object>> result =
                    ageGraphService.findEdgesByObjectType("source-type-1", "ontology-1");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("JdbcTemplate 抛出异常时应返回空列表（不抛异常）")
        void findEdgesByObjectType_shouldReturnEmptyList_whenJdbcTemplateThrows() {
            when(jdbcTemplate.queryForList(anyString()))
                    .thenThrow(new RuntimeException("DB error"));

            List<Map<String, Object>> result =
                    ageGraphService.findEdgesByObjectType("source-type-1", "ontology-1");

            assertThat(result).isEmpty();
        }
    }

    // ==================== initializeGraph ====================

    @Nested
    @DisplayName("initializeGraph 方法")
    class InitializeGraphTests {

        @Test
        @DisplayName("图不存在时应加载 AGE 扩展、设置搜索路径并创建图")
        void initializeGraph_shouldCreateGraph_whenNotExists() {
            when(jdbcTemplate.queryForObject(anyString(), any(Class.class))).thenReturn(0);

            ageGraphService.initializeGraph();

            verify(jdbcTemplate).execute("LOAD 'age'");
            verify(jdbcTemplate).execute("SET search_path = ag_catalog, \"$user\", public");
            verify(jdbcTemplate).queryForObject(
                    contains("SELECT count(*) FROM ag_catalog.ag_graph WHERE name = 'ontology_graph'"),
                    any(Class.class));
            verify(jdbcTemplate).queryForList(
                    contains("SELECT * FROM ag_catalog.create_graph('ontology_graph')"));
        }

        @Test
        @DisplayName("图已存在时应跳过创建，不调用 create_graph")
        void initializeGraph_shouldSkipCreation_whenGraphExists() {
            when(jdbcTemplate.queryForObject(anyString(), any(Class.class))).thenReturn(1);

            ageGraphService.initializeGraph();

            verify(jdbcTemplate).execute("LOAD 'age'");
            verify(jdbcTemplate).execute("SET search_path = ag_catalog, \"$user\", public");
            verify(jdbcTemplate, never()).queryForList(contains("create_graph"));
        }

        @Test
        @DisplayName("count 查询返回 null 时应按不存在处理并创建图")
        void initializeGraph_shouldCreateGraph_whenCountIsNull() {
            when(jdbcTemplate.queryForObject(anyString(), any(Class.class))).thenReturn(null);

            ageGraphService.initializeGraph();

            verify(jdbcTemplate).queryForList(contains("create_graph"));
        }

        @Test
        @DisplayName("JdbcTemplate 抛出异常时应静默处理（不抛异常）")
        void initializeGraph_shouldHandleExceptionGracefully() {
            when(jdbcTemplate.queryForObject(anyString(), any(Class.class)))
                    .thenThrow(new RuntimeException("Apache AGE not available"));

            // 不应抛出任何异常
            ageGraphService.initializeGraph();

            verify(jdbcTemplate).execute("LOAD 'age'");
        }
    }

    // ==================== createGraphForOntology ====================

    @Nested
    @DisplayName("createGraphForOntology 方法")
    class CreateGraphForOntologyTests {

        @Test
        @DisplayName("应为指定 ontologyId 创建独立图（连字符替换为下划线）")
        void createGraphForOntology_shouldCreateGraph() {
            when(jdbcTemplate.queryForObject(anyString(), any(Class.class))).thenReturn(0);

            ageGraphService.createGraphForOntology("ontology-42");

            verify(jdbcTemplate).execute("LOAD 'age'");
            verify(jdbcTemplate).execute("SET search_path = ag_catalog, \"$user\", public");
            verify(jdbcTemplate).queryForList(
                    contains("SELECT * FROM ag_catalog.create_graph('ontology_ontology_42')"));
        }

        @Test
        @DisplayName("图已存在时应跳过创建")
        void createGraphForOntology_shouldSkip_whenGraphExists() {
            when(jdbcTemplate.queryForObject(anyString(), any(Class.class))).thenReturn(1);

            ageGraphService.createGraphForOntology("ontology-42");

            verify(jdbcTemplate, never()).queryForList(contains("create_graph"));
        }

        @Test
        @DisplayName("JdbcTemplate 抛出异常时应静默处理（不抛异常）")
        void createGraphForOntology_shouldHandleExceptionGracefully() {
            when(jdbcTemplate.queryForObject(anyString(), any(Class.class)))
                    .thenThrow(new RuntimeException("Apache AGE not available"));

            ageGraphService.createGraphForOntology("ontology-42");

            verify(jdbcTemplate).execute("LOAD 'age'");
        }
    }

    // ==================== getGraphName 逻辑验证 ====================

    @Nested
    @DisplayName("getGraphName 图名构造逻辑")
    class GetGraphNameTests {

        @Test
        @DisplayName("ontologyId 为 null 时应返回默认图名 ontology_graph")
        void getGraphName_shouldReturnDefault_whenOntologyIdIsNull() {
            // 通过 deleteEdge 间接验证，因为 ontologyId 为 null 时走默认图名
            ageGraphService.deleteEdge("rel-x", null);

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue()).contains("cypher('ontology_graph'");
        }

        @Test
        @DisplayName("ontologyId 包含连字符时应替换为下划线")
        void getGraphName_shouldReplaceHyphensWithUnderscores() {
            ageGraphService.deleteEdge("rel-x", "my-ontology-123");

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue()).contains("cypher('ontology_my_ontology_123'");
        }

        @Test
        @DisplayName("ontologyId 不含连字符时直接拼接")
        void getGraphName_shouldConcatDirectly_whenNoHyphens() {
            ageGraphService.deleteEdge("rel-x", "simpleId");

            ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
            verify(jdbcTemplate).queryForList(sqlCaptor.capture());
            assertThat(sqlCaptor.getValue()).contains("cypher('ontology_simpleId'");
        }
    }
}
