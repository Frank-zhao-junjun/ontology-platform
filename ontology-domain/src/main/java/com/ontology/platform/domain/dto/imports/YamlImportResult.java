package com.ontology.platform.domain.dto.imports;

import com.ontology.platform.domain.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * YAML 导入结果：包含解析后生成的所有领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YamlImportResult {

    /** 本体 */
    private Ontology ontology;

    /** 对象类型列表 */
    @Builder.Default
    private List<ObjectType> objectTypes = new ArrayList<>();

    /** 状态机列表 */
    @Builder.Default
    private List<StateMachine> stateMachines = new ArrayList<>();

    /** 动作定义列表 */
    @Builder.Default
    private List<ActionDefinition> actions = new ArrayList<>();

    /** 领域事件列表 */
    @Builder.Default
    private List<DomainEvent> domainEvents = new ArrayList<>();

    /** 导入统计 */
    private int totalEntities;
    private List<String> warnings;
}
