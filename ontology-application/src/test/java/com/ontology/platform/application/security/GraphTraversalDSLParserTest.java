package com.ontology.platform.application.security;

import com.ontology.platform.common.enums.FilterOperator;
import com.ontology.platform.common.enums.ReturnFormat;
import com.ontology.platform.common.enums.TraversalDirection;
import com.ontology.platform.common.exception.ValidationException;
import com.ontology.platform.domain.service.GraphWhitelistService;
import com.ontology.platform.domain.vo.traversal.CypherQuery;
import com.ontology.platform.domain.vo.traversal.GraphTraversalRequest;
import com.ontology.platform.domain.vo.traversal.TraversalFilter;
import com.ontology.platform.domain.vo.traversal.TraversalFilterCondition;
import com.ontology.platform.domain.vo.traversal.TraversalPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * GraphTraversalDSLParser 单元测试
 * 
 * 测试DSL解析器的各项功能：
 * 1. 必填字段验证
 * 2. UUID格式校验
 * 3. 白名单验证
 * 4. 深度限制
 * 5. 结果限制
 * 6. Cypher查询生成
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GraphTraversalDSLParser Tests")
class GraphTraversalDSLParserTest {
    
    @Mock
    private FilterSecurityValidator filterValidator;
    
    @Mock
    private GraphWhitelistService whitelistService;
    
    private GraphTraversalDSLParser parser;
    
    @BeforeEach
    void setUp() {
        parser = new GraphTraversalDSLParser(filterValidator, whitelistService);
        
        // 默认白名单服务行为
        when(whitelistService.isObjectTypeAllowed(anyString())).thenReturn(true);
        when(whitelistService.isRelationTypeAllowed(anyString())).thenReturn(true);
        when(whitelistService.normalizeObjectType(anyString())).thenAnswer(i -> 
            i.getArgument(0).toString().toLowerCase());
        when(whitelistService.normalizeRelationType(anyString())).thenAnswer(i -> 
            i.getArgument(0).toString().toUpperCase());
    }
    
    // ==================== 必填字段验证测试 ====================
    
    @Nested
    @DisplayName("Required Field Validation Tests")
    class RequiredFieldValidationTests {
        
        @Test
        @DisplayName("Should reject null startObjectType")
        void shouldRejectNullStartObjectType() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .build();
            
            ValidationException ex = assertThrows(ValidationException.class, 
                () -> parser.parse(request));
            assertTrue(ex.getMessage().contains("startObjectType"));
        }
        
        @Test
        @DisplayName("Should reject blank startObjectType")
        void shouldRejectBlankStartObjectType() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("   ")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .build();
            
            ValidationException ex = assertThrows(ValidationException.class, 
                () -> parser.parse(request));
            assertTrue(ex.getMessage().contains("startObjectType"));
        }
        
        @Test
        @DisplayName("Should reject null startObjectId")
        void shouldRejectNullStartObjectId() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .build();
            
            ValidationException ex = assertThrows(ValidationException.class, 
                () -> parser.parse(request));
            assertTrue(ex.getMessage().contains("startObjectId"));
        }
        
        @Test
        @DisplayName("Should reject invalid UUID format")
        void shouldRejectInvalidUUIDFormat() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("invalid-uuid")
                    .build();
            
            ValidationException ex = assertThrows(ValidationException.class, 
                () -> parser.parse(request));
            assertTrue(ex.getMessage().contains("UUID"));
        }
    }
    
    // ==================== 白名单验证测试 ====================
    
    @Nested
    @DisplayName("Whitelist Validation Tests")
    class WhitelistValidationTests {
        
        @Test
        @DisplayName("Should reject disallowed object type")
        void shouldRejectDisallowedObjectType() {
            when(whitelistService.isObjectTypeAllowed("forbidden")).thenReturn(false);
            
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("forbidden")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .build();
            
            ValidationException ex = assertThrows(ValidationException.class, 
                () -> parser.parse(request));
            assertTrue(ex.getMessage().contains("Object type"));
        }
        
        @Test
        @DisplayName("Should reject disallowed relation type in path")
        void shouldRejectDisallowedRelationType() {
            when(whitelistService.isRelationTypeAllowed("FORBIDDEN_REL")).thenReturn(false);
            
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .path(List.of(
                        TraversalPath.builder()
                                .relationType("FORBIDDEN_REL")
                                .build()
                    ))
                    .build();
            
            ValidationException ex = assertThrows(ValidationException.class, 
                () -> parser.parse(request));
            assertTrue(ex.getMessage().contains("Relation type"));
        }
    }
    
    // ==================== 深度限制测试 ====================
    
    @Nested
    @DisplayName("Depth Limit Tests")
    class DepthLimitTests {
        
        @Test
        @DisplayName("Should accept depth within limit")
        void shouldAcceptDepthWithinLimit() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .maxDepth(3)
                    .build();
            
            assertDoesNotThrow(() -> parser.parse(request));
        }
        
        @Test
        @DisplayName("Should cap depth at maximum")
        void shouldCapDepthAtMaximum() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .maxDepth(10) // 超过最大值5
                    .build();
            
            CypherQuery result = parser.parse(request);
            assertNotNull(result);
            // 验证参数中限制了深度
            assertTrue(result.params().containsKey("maxDepth"));
        }
        
        @Test
        @DisplayName("Should use default depth when not specified")
        void shouldUseDefaultDepthWhenNotSpecified() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .build();
            
            CypherQuery result = parser.parse(request);
            assertNotNull(result);
        }
    }
    
    // ==================== 结果限制测试 ====================
    
    @Nested
    @DisplayName("Result Limit Tests")
    class ResultLimitTests {
        
        @Test
        @DisplayName("Should accept limit within maximum")
        void shouldAcceptLimitWithinMaximum() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .limit(500)
                    .build();
            
            assertDoesNotThrow(() -> parser.parse(request));
        }
        
        @Test
        @DisplayName("Should cap limit at maximum")
        void shouldCapLimitAtMaximum() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .limit(5000) // 超过最大值1000
                    .build();
            
            CypherQuery result = parser.parse(request);
            assertNotNull(result);
            // 验证参数中限制了结果数
            assertTrue(result.params().containsKey("limit"));
        }
    }
    
    // ==================== Cypher生成测试 ====================
    
    @Nested
    @DisplayName("Cypher Generation Tests")
    class CypherGenerationTests {
        
        @Test
        @DisplayName("Should generate valid basic Cypher query")
        void shouldGenerateValidBasicCypherQuery() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .maxDepth(3)
                    .limit(100)
                    .build();
            
            CypherQuery result = parser.parse(request);
            
            assertNotNull(result);
            assertNotNull(result.cypher());
            assertNotNull(result.params());
            
            // 验证Cypher包含必要的子句
            assertTrue(result.cypher().contains("MATCH"));
            assertTrue(result.cypher().contains("RETURN"));
            assertTrue(result.cypher().contains("LIMIT"));
        }
        
        @Test
        @DisplayName("Should include parameters in result")
        void shouldIncludeParametersInResult() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .build();
            
            CypherQuery result = parser.parse(request);
            
            assertTrue(result.params().containsKey("startObjectId"));
            assertTrue(result.params().containsKey("startObjectType"));
            assertEquals("550e8400-e29b-41d4-a716-446655440000", 
                result.params().get("startObjectId"));
        }
        
        @Test
        @DisplayName("Should generate ORDER BY clause")
        void shouldGenerateOrderByClause() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .build();
            
            CypherQuery result = parser.parse(request);
            
            assertTrue(result.cypher().contains("ORDER BY"));
        }
    }
    
    // ==================== 路径解析测试 ====================
    
    @Nested
    @DisplayName("Path Parsing Tests")
    class PathParsingTests {
        
        @Test
        @DisplayName("Should handle empty path")
        void shouldHandleEmptyPath() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .path(List.of())
                    .build();
            
            assertDoesNotThrow(() -> parser.parse(request));
        }
        
        @Test
        @DisplayName("Should handle null path")
        void shouldHandleNullPath() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .path(null)
                    .build();
            
            assertDoesNotThrow(() -> parser.parse(request));
        }
        
        @Test
        @DisplayName("Should normalize relation types to uppercase")
        void shouldNormalizeRelationTypes() {
            when(whitelistService.normalizeRelationType("owns")).thenReturn("OWNS");
            
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .path(List.of(
                        TraversalPath.builder()
                                .relationType("owns")
                                .build()
                    ))
                    .build();
            
            CypherQuery result = parser.parse(request);
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Should skip null path segments")
        void shouldSkipNullPathSegments() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .path(List.of(
                        null,
                        TraversalPath.builder()
                                .relationType("owns")
                                .build()
                    ))
                    .build();
            
            assertDoesNotThrow(() -> parser.parse(request));
        }
    }
    
    // ==================== 返回格式测试 ====================
    
    @Nested
    @DisplayName("Return Format Tests")
    class ReturnFormatTests {
        
        @Test
        @DisplayName("Should support GRAPH format")
        void shouldSupportGraphFormat() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .returnFormat(ReturnFormat.GRAPH)
                    .build();
            
            CypherQuery result = parser.parse(request);
            assertNotNull(result);
            assertTrue(result.cypher().contains("path"));
        }
        
        @Test
        @DisplayName("Should support TREE format")
        void shouldSupportTreeFormat() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .returnFormat(ReturnFormat.TREE)
                    .build();
            
            CypherQuery result = parser.parse(request);
            assertNotNull(result);
        }
        
        @Test
        @DisplayName("Should support FLAT format")
        void shouldSupportFlatFormat() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .returnFormat(ReturnFormat.FLAT)
                    .build();
            
            GraphTraversalDSLParser.CypherQuery result = parser.parse(request);
            assertNotNull(result);
        }
    }
    
    // ==================== 方向枚举测试 ====================
    
    @Nested
    @DisplayName("Direction Enum Tests")
    class DirectionTests {
        
        @Test
        @DisplayName("Should support OUTGOING direction")
        void shouldSupportOutgoingDirection() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .direction(TraversalDirection.OUTGOING)
                    .build();
            
            assertDoesNotThrow(() -> parser.parse(request));
        }
        
        @Test
        @DisplayName("Should support INCOMING direction")
        void shouldSupportIncomingDirection() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .direction(TraversalDirection.INCOMING)
                    .build();
            
            assertDoesNotThrow(() -> parser.parse(request));
        }
        
        @Test
        @DisplayName("Should support BOTH direction")
        void shouldSupportBothDirection() {
            GraphTraversalRequest request = GraphTraversalRequest.builder()
                    .startObjectType("customer")
                    .startObjectId("550e8400-e29b-41d4-a716-446655440000")
                    .direction(TraversalDirection.BOTH)
                    .build();
            
            assertDoesNotThrow(() -> parser.parse(request));
        }
    }
}
