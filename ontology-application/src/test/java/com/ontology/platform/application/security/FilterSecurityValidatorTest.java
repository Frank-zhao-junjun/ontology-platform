package com.ontology.platform.application.security;

import com.ontology.platform.common.enums.FilterOperator;
import com.ontology.platform.common.exception.ValidationException;
import com.ontology.platform.domain.vo.traversal.TraversalFilter;
import com.ontology.platform.domain.vo.traversal.TraversalFilterCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FilterSecurityValidator 单元测试
 * 
 * 测试安全校验器的各项功能：
 * 1. 操作符枚举限制
 * 2. 字段名格式验证
 * 3. 值长度限制
 * 4. 数组长度限制
 * 5. 危险字符检测
 */
@DisplayName("FilterSecurityValidator Tests")
class FilterSecurityValidatorTest {
    
    private FilterSecurityValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new FilterSecurityValidator();
    }
    
    // ==================== 基础验证测试 ====================
    
    @Nested
    @DisplayName("Basic Validation Tests")
    class BasicValidationTests {
        
        @Test
        @DisplayName("Should return empty list for null filters")
        void shouldReturnEmptyListForNullFilters() {
            List<TraversalFilter> result = validator.validateFilters(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should return empty list for empty filters")
        void shouldReturnEmptyListForEmptyFilters() {
            List<TraversalFilter> result = validator.validateFilters(List.of());
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("Should validate null filter")
        void shouldValidateNullFilter() {
            TraversalFilter result = validator.validateFilter(null);
            assertNotNull(result);
            assertEquals(0, result.getDepth());
            assertTrue(result.getConditions().isEmpty());
        }
    }
    
    // ==================== 字段名验证测试 ====================
    
    @Nested
    @DisplayName("Field Name Validation Tests")
    class FieldNameValidationTests {
        
        @Test
        @DisplayName("Should accept valid field names")
        void shouldAcceptValidFieldNames() {
            List<String> validFields = List.of(
                "name", "user_name", "userName", "_private",
                "NAME", "UserName", "_Private123"
            );
            
            for (String field : validFields) {
                assertDoesNotThrow(() -> {
                    TraversalFilterCondition condition = TraversalFilterCondition.builder()
                            .field(field)
                            .operator(FilterOperator.eq)
                            .value("test")
                            .build();
                    
                    validator.validateFilter(TraversalFilter.builder()
                            .conditions(List.of(condition))
                            .logic("AND")
                            .build());
                }, "Field should be valid: " + field);
            }
        }
        
        @Test
        @DisplayName("Should reject field names with invalid characters")
        void shouldRejectFieldNamesWithInvalidCharacters() {
            List<String> invalidFields = List.of(
                "user-name", "user.name", "user name",
                "1field", "field@name", "field#name"
            );
            
            for (String field : invalidFields) {
                assertThrows(ValidationException.class, () -> {
                    TraversalFilterCondition condition = TraversalFilterCondition.builder()
                            .field(field)
                            .operator(FilterOperator.eq)
                            .value("test")
                            .build();
                    
                    validator.validateFilter(TraversalFilter.builder()
                            .conditions(List.of(condition))
                            .logic("AND")
                            .build());
                }, "Field should be invalid: " + field);
            }
        }
        
        @Test
        @DisplayName("Should reject empty field names")
        void shouldRejectEmptyFieldNames() {
            assertThrows(ValidationException.class, () -> {
                TraversalFilterCondition condition = TraversalFilterCondition.builder()
                        .field("")
                        .operator(FilterOperator.eq)
                        .value("test")
                        .build();
                
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(List.of(condition))
                        .logic("AND")
                        .build());
            });
        }
        
        @Test
        @DisplayName("Should reject null field names")
        void shouldRejectNullFieldNames() {
            assertThrows(ValidationException.class, () -> {
                TraversalFilterCondition condition = TraversalFilterCondition.builder()
                        .field(null)
                        .operator(FilterOperator.eq)
                        .value("test")
                        .build();
                
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(List.of(condition))
                        .logic("AND")
                        .build());
            });
        }
    }
    
    // ==================== 操作符验证测试 ====================
    
    @Nested
    @DisplayName("Operator Validation Tests")
    class OperatorValidationTests {
        
        @Test
        @DisplayName("Should accept all allowed operators")
        void shouldAcceptAllAllowedOperators() {
            for (FilterOperator op : FilterOperator.values()) {
                assertDoesNotThrow(() -> {
                    TraversalFilterCondition condition = TraversalFilterCondition.builder()
                            .field("status")
                            .operator(op)
                            .value(op == FilterOperator.isNull || op == FilterOperator.isNotNull 
                                   ? null : "test")
                            .build();
                    
                    validator.validateFilter(TraversalFilter.builder()
                            .conditions(List.of(condition))
                            .logic("AND")
                            .build());
                }, "Operator should be valid: " + op);
            }
        }
        
        @Test
        @DisplayName("Should reject null operator")
        void shouldRejectNullOperator() {
            assertThrows(ValidationException.class, () -> {
                TraversalFilterCondition condition = TraversalFilterCondition.builder()
                        .field("status")
                        .operator(null)
                        .value("test")
                        .build();
                
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(List.of(condition))
                        .logic("AND")
                        .build());
            });
        }
    }
    
    // ==================== 逻辑运算符验证测试 ====================
    
    @Nested
    @DisplayName("Logic Operator Validation Tests")
    class LogicOperatorTests {
        
        @Test
        @DisplayName("Should accept AND logic")
        void shouldAcceptAndLogic() {
            assertDoesNotThrow(() -> {
                validator.validateFilter(TraversalFilter.builder()
                        .logic("AND")
                        .conditions(List.of())
                        .build());
            });
        }
        
        @Test
        @DisplayName("Should accept OR logic")
        void shouldAcceptOrLogic() {
            assertDoesNotThrow(() -> {
                validator.validateFilter(TraversalFilter.builder()
                        .logic("OR")
                        .conditions(List.of())
                        .build());
            });
        }
        
        @Test
        @DisplayName("Should reject invalid logic")
        void shouldRejectInvalidLogic() {
            assertThrows(ValidationException.class, () -> {
                validator.validateFilter(TraversalFilter.builder()
                        .logic("XOR")
                        .conditions(List.of())
                        .build());
            });
        }
    }
    
    // ==================== 值长度验证测试 ====================
    
    @Nested
    @DisplayName("Value Length Validation Tests")
    class ValueLengthValidationTests {
        
        @Test
        @DisplayName("Should accept value within length limit")
        void shouldAcceptValueWithinLimit() {
            String validValue = "a".repeat(500);
            assertDoesNotThrow(() -> {
                TraversalFilterCondition condition = TraversalFilterCondition.builder()
                        .field("description")
                        .operator(FilterOperator.eq)
                        .value(validValue)
                        .build();
                
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(List.of(condition))
                        .logic("AND")
                        .build());
            });
        }
        
        @Test
        @DisplayName("Should reject value exceeding length limit")
        void shouldRejectValueExceedingLimit() {
            String tooLongValue = "a".repeat(1001);
            assertThrows(ValidationException.class, () -> {
                TraversalFilterCondition condition = TraversalFilterCondition.builder()
                        .field("description")
                        .operator(FilterOperator.eq)
                        .value(tooLongValue)
                        .build();
                
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(List.of(condition))
                        .logic("AND")
                        .build());
            });
        }
    }
    
    // ==================== 数组值验证测试 ====================
    
    @Nested
    @DisplayName("Array Value Validation Tests")
    class ArrayValueValidationTests {
        
        @Test
        @DisplayName("Should accept valid array for IN operator")
        void shouldAcceptValidArrayForInOperator() {
            List<Object> validArray = List.of("a", "b", "c");
            assertDoesNotThrow(() -> {
                TraversalFilterCondition condition = TraversalFilterCondition.builder()
                        .field("status")
                        .operator(FilterOperator.in)
                        .value(validArray)
                        .build();
                
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(List.of(condition))
                        .logic("AND")
                        .build());
            });
        }
        
        @Test
        @DisplayName("Should reject non-array for IN operator")
        void shouldRejectNonArrayForInOperator() {
            assertThrows(ValidationException.class, () -> {
                TraversalFilterCondition condition = TraversalFilterCondition.builder()
                        .field("status")
                        .operator(FilterOperator.in)
                        .value("single_value")
                        .build();
                
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(List.of(condition))
                        .logic("AND")
                        .build());
            });
        }
        
        @Test
        @DisplayName("Should reject array exceeding size limit")
        void shouldRejectArrayExceedingSizeLimit() {
            List<Object> tooLargeArray = new ArrayList<>();
            for (int i = 0; i < 101; i++) {
                tooLargeArray.add("value" + i);
            }
            
            assertThrows(ValidationException.class, () -> {
                TraversalFilterCondition condition = TraversalFilterCondition.builder()
                        .field("status")
                        .operator(FilterOperator.in)
                        .value(tooLargeArray)
                        .build();
                
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(List.of(condition))
                        .logic("AND")
                        .build());
            });
        }
    }
    
    // ==================== 特殊值验证测试 ====================
    
    @Nested
    @DisplayName("Special Value Validation Tests")
    class SpecialValueValidationTests {
        
        @Test
        @DisplayName("Should not require value for isNull operator")
        void shouldNotRequireValueForIsNull() {
            assertDoesNotThrow(() -> {
                TraversalFilterCondition condition = TraversalFilterCondition.builder()
                        .field("deletedAt")
                        .operator(FilterOperator.isNull)
                        .value(null)
                        .build();
                
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(List.of(condition))
                        .logic("AND")
                        .build());
            });
        }
        
        @Test
        @DisplayName("Should require value for eq operator")
        void shouldRequireValueForEq() {
            assertThrows(ValidationException.class, () -> {
                TraversalFilterCondition condition = TraversalFilterCondition.builder()
                        .field("status")
                        .operator(FilterOperator.eq)
                        .value(null)
                        .build();
                
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(List.of(condition))
                        .logic("AND")
                        .build());
            });
        }
        
        @Test
        @DisplayName("Should accept number values")
        void shouldAcceptNumberValues() {
            assertDoesNotThrow(() -> {
                TraversalFilterCondition condition = TraversalFilterCondition.builder()
                        .field("age")
                        .operator(FilterOperator.gt)
                        .value(18)
                        .build();
                
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(List.of(condition))
                        .logic("AND")
                        .build());
            });
        }
        
        @Test
        @DisplayName("Should accept boolean values")
        void shouldAcceptBooleanValues() {
            assertDoesNotThrow(() -> {
                TraversalFilterCondition condition = TraversalFilterCondition.builder()
                        .field("active")
                        .operator(FilterOperator.eq)
                        .value(true)
                        .build();
                
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(List.of(condition))
                        .logic("AND")
                        .build());
            });
        }
    }
    
    // ==================== 条件数量限制测试 ====================
    
    @Nested
    @DisplayName("Condition Count Limit Tests")
    class ConditionCountLimitTests {
        
        @Test
        @DisplayName("Should accept conditions within limit")
        void shouldAcceptConditionsWithinLimit() {
            List<TraversalFilterCondition> conditions = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                conditions.add(TraversalFilterCondition.builder()
                        .field("field" + i)
                        .operator(FilterOperator.eq)
                        .value("value" + i)
                        .build());
            }
            
            assertDoesNotThrow(() -> {
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(conditions)
                        .logic("AND")
                        .build());
            });
        }
        
        @Test
        @DisplayName("Should reject conditions exceeding limit")
        void shouldRejectConditionsExceedingLimit() {
            List<TraversalFilterCondition> conditions = new ArrayList<>();
            for (int i = 0; i < 21; i++) {
                conditions.add(TraversalFilterCondition.builder()
                        .field("field" + i)
                        .operator(FilterOperator.eq)
                        .value("value" + i)
                        .build());
            }
            
            assertThrows(ValidationException.class, () -> {
                validator.validateFilter(TraversalFilter.builder()
                        .conditions(conditions)
                        .logic("AND")
                        .build());
            });
        }
    }
    
    // ==================== 白名单验证测试 ====================
    
    @Nested
    @DisplayName("Whitelist Validation Tests")
    class WhitelistValidationTests {
        
        @Test
        @DisplayName("Should allow field when whitelist is null")
        void shouldAllowFieldWhenWhitelistNull() {
            assertTrue(validator.isFieldInWhitelist("field", null));
        }
        
        @Test
        @DisplayName("Should allow field when whitelist is empty")
        void shouldAllowFieldWhenWhitelistEmpty() {
            assertTrue(validator.isFieldInWhitelist("field", Set.of()));
        }
        
        @Test
        @DisplayName("Should allow field in whitelist")
        void shouldAllowFieldInWhitelist() {
            Set<String> whitelist = Set.of("name", "status", "createdAt");
            assertTrue(validator.isFieldInWhitelist("name", whitelist));
        }
        
        @Test
        @DisplayName("Should reject field not in whitelist")
        void shouldRejectFieldNotInWhitelist() {
            Set<String> whitelist = Set.of("name", "status", "createdAt");
            assertFalse(validator.isFieldInWhitelist("password", whitelist));
        }
    }
    
    // ==================== 危险字符检测测试 ====================
    
    @Nested
    @DisplayName("Dangerous Character Detection Tests")
    class DangerousCharacterTests {
        
        @Test
        @DisplayName("Should accept normal string values")
        void shouldAcceptNormalStrings() {
            List<String> normalValues = List.of(
                "John Doe", "user@example.com", "123 Main Street"
            );
            
            for (String value : normalValues) {
                assertDoesNotThrow(() -> {
                    TraversalFilterCondition condition = TraversalFilterCondition.builder()
                            .field("field")
                            .operator(FilterOperator.eq)
                            .value(value)
                            .build();
                    
                    validator.validateFilter(TraversalFilter.builder()
                            .conditions(List.of(condition))
                            .logic("AND")
                            .build());
                }, "Value should be accepted: " + value);
            }
        }
        
        @Test
        @DisplayName("Should accept valid wildcard patterns")
        void shouldAcceptValidWildcardPatterns() {
            List<String> validPatterns = List.of(
                "*test*", "*test", "test*"
            );
            
            for (String pattern : validPatterns) {
                assertDoesNotThrow(() -> {
                    TraversalFilterCondition condition = TraversalFilterCondition.builder()
                            .field("field")
                            .operator(FilterOperator.like)
                            .value(pattern)
                            .build();
                    
                    validator.validateFilter(TraversalFilter.builder()
                            .conditions(List.of(condition))
                            .logic("AND")
                            .build());
                }, "Pattern should be accepted: " + pattern);
            }
        }
    }
}
