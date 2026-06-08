package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.common.enums.PropertyDataType;
import com.ontology.platform.domain.vo.Property;
import com.ontology.platform.domain.vo.PropertyConstraint;
import com.ontology.platform.infrastructure.persistence.PropertyPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 属性持久化对象转换器
 * Property PO <-> VO Converter
 */
@Slf4j
@Component
public class PropertyConverter {

    /**
     * PO转换为VO
     */
    public Property toVO(PropertyPO po) {
        if (po == null) {
            return null;
        }
        
        return Property.builder()
                .id(po.getId())
                .objectTypeId(po.getObjectTypeId())
                .name(po.getName())
                .displayName(po.getDisplayName())
                .description(po.getDescription())
                .dataType(po.getDataTypeEnum())
                .isComputed(po.getIsComputed() != null ? po.getIsComputed() : false)
                .isRequired(po.getIsRequired() != null ? po.getIsRequired() : false)
                .isUnique(po.getIsUnique() != null ? po.getIsUnique() : false)
                .isSearchable(po.getIsSearchable() != null ? po.getIsSearchable() : true)
                .isSortable(po.getIsSortable() != null ? po.getIsSortable() : true)
                .defaultValue(po.getDefaultValueAsObject())
                .sortOrder(po.getSortOrder() != null ? po.getSortOrder() : 0)
                .constraints(parseConstraints(po))
                .nestedProperties(new ArrayList<>())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .build();
    }

    /**
     * VO转换为PO
     */
    public PropertyPO toPO(Property vo) {
        if (vo == null) {
            return null;
        }
        
        PropertyPO po = PropertyPO.builder()
                .id(vo.getId())
                .objectTypeId(vo.getObjectTypeId())
                .name(vo.getName())
                .displayName(vo.getDisplayName())
                .description(vo.getDescription())
                .dataType(vo.getDataType() != null ? vo.getDataType().getValue() : null)
                .isComputed(vo.isComputed())
                .isRequired(vo.isRequired())
                .isUnique(vo.isUnique())
                .isSearchable(vo.isSearchable())
                .isSortable(vo.isSortable())
                .sortOrder(vo.getSortOrder())
                .createdAt(vo.getCreatedAt())
                .updatedAt(vo.getUpdatedAt())
                .build();
        
        po.setDefaultValueObject(vo.getDefaultValue());
        po.setExtendedDataMap(buildExtendedData(vo));
        
        return po;
    }

    /**
     * PO列表转换为VO列表
     */
    public List<Property> toVOList(List<PropertyPO> poList) {
        if (poList == null) {
            return List.of();
        }
        return poList.stream()
                .map(this::toVO)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * VO列表转换为PO列表
     */
    public List<PropertyPO> toPOList(List<Property> voList) {
        if (voList == null) {
            return List.of();
        }
        return voList.stream()
                .map(this::toPO)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 解析约束
     */
    @SuppressWarnings("unchecked")
    private List<PropertyConstraint> parseConstraints(PropertyPO po) {
        Map<String, Object> extendedData = po.getExtendedDataMap();
        if (extendedData == null || !extendedData.containsKey("constraints")) {
            return new ArrayList<>();
        }
        
        try {
            List<Map<String, Object>> constraintList = (List<Map<String, Object>>) extendedData.get("constraints");
            List<PropertyConstraint> constraints = new ArrayList<>();
            
            for (Map<String, Object> constraintData : constraintList) {
                PropertyConstraint constraint = parseConstraint(constraintData);
                if (constraint != null) {
                    constraints.add(constraint);
                }
            }
            
            return constraints;
        } catch (Exception e) {
            log.warn("Failed to parse constraints from property {}: {}", po.getId(), e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 解析单个约束
     */
    private PropertyConstraint parseConstraint(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        
        String typeStr = (String) data.get("type");
        if (typeStr == null) {
            return null;
        }
        
        PropertyConstraint.ConstraintType type;
        try {
            type = PropertyConstraint.ConstraintType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown constraint type: {}", typeStr);
            return null;
        }
        
        Object value = data.get("value");
        String errorMessage = (String) data.get("errorMessage");
        
        Object convertedValue = convertConstraintValue(type, value);
        
        return PropertyConstraint.builder()
                .id((String) data.get("id"))
                .type(type)
                .value(convertedValue)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 转换约束值类型
     */
    private Object convertConstraintValue(PropertyConstraint.ConstraintType type, Object value) {
        if (value == null) {
            return null;
        }
        
        return switch (type) {
            case MIN_VALUE, MAX_VALUE -> {
                if (value instanceof Number) {
                    yield new BigDecimal(value.toString());
                } else if (value instanceof String) {
                    yield new BigDecimal((String) value);
                }
                yield value;
            }
            case MIN_LENGTH, MAX_LENGTH -> {
                if (value instanceof Number) {
                    yield ((Number) value).intValue();
                } else if (value instanceof String) {
                    yield Integer.parseInt((String) value);
                }
                yield value;
            }
            case ENUM_VALUES -> {
                if (value instanceof List) {
                    yield new ArrayList<>((List<String>) value);
                }
                yield value;
            }
            default -> value;
        };
    }

    /**
     * 构建扩展数据
     */
    private Map<String, Object> buildExtendedData(Property vo) {
        java.util.HashMap<String, Object> extendedData = new java.util.HashMap<>();
        
        if (vo.getConstraints() != null && !vo.getConstraints().isEmpty()) {
            List<Map<String, Object>> constraintList = new ArrayList<>();
            for (PropertyConstraint constraint : vo.getConstraints()) {
                constraintList.add(Map.of(
                        "id", constraint.getId() != null ? constraint.getId() : "",
                        "type", constraint.getType().name(),
                        "value", constraint.getValue(),
                        "errorMessage", constraint.getErrorMessage() != null ? constraint.getErrorMessage() : ""
                ));
            }
            extendedData.put("constraints", constraintList);
        }
        
        return extendedData;
    }
}
