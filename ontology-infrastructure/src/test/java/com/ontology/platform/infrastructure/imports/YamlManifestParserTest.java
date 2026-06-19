package com.ontology.platform.infrastructure.imports;

import com.ontology.platform.domain.dto.imports.YamlImportResult;
import com.ontology.platform.domain.dto.imports.YamlManifest;
import com.ontology.platform.domain.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * YAML Manifest 解析 + 转换 集成测试
 */
class YamlManifestParserTest {

    private final YamlManifestParser parser = new YamlManifestParser();
    private final YamlManifestConverter converter = new YamlManifestConverter();

    /** 完整 manufacturing-manifest.yaml 的 Java 字符串 */
    private static final String MANIFEST_YAML = """
            apiVersion: ontology.platform/v1
            kind: OntologyManifest
            metadata:
              id: manufacturing-ontology
              version: 0.1.0
              name: 生产制造本体
              displayName: 生产制造本体
              description: MVP 制造域参考模型（生产订单 / 物料 / BOM / 工艺路线）
              boundedContext: 生产制造
              domainTags:
                - 生产制造
              compiledAt: "2026-06-04T00:00:00.000Z"
              source: ontology-designer
              status: draft
            spec:
              semantic:
                boundedContext:
                  id: bc-manufacturing
                  name: 生产制造
                  nameEn: Manufacturing
                  description: 生产执行与工单生命周期
                businessScenarios:
                  - id: scenario-mts
                    name: 面向库存生产
                    nameEn: MTS
                    description: 按库存补货驱动生产
                    applicableObjectTypeIds:
                      - production-order
                  - id: scenario-mto
                    name: 面向订单生产
                    nameEn: MTO
                    description: 严格按销售订单驱动
                    applicableObjectTypeIds:
                      - production-order
                valueObjects:
                  - id: vo-quantity
                    name: 数量
                    nameEn: Quantity
                    properties:
                      - id: vo-q-amount
                        name: 数值
                        nameEn: amount
                        dataType: decimal
                        required: true
                      - id: vo-q-unit
                        name: 单位
                        nameEn: unit
                        dataType: string
                        required: true
                objectTypes:
                  - id: production-order
                    name: 生产订单
                    nameEn: ProductionOrder
                    kind: aggregate_root
                    businessScenarioIds:
                      - scenario-mts
                      - scenario-mto
                    description: 核心生产执行单据
                    properties:
                      - id: attr-order-id
                        name: 生产订单号
                        nameEn: order_id
                        dataType: string
                        required: true
                      - id: attr-order-type
                        name: 订单类型
                        nameEn: order_type
                        dataType: enum
                        enumValues: [MTS, MTO]
                        required: true
                      - id: attr-status
                        name: 订单状态
                        nameEn: status
                        dataType: enum
                        enumValues: [CREATED, RELEASED, IN_PROGRESS, REPORTED, PRODUCED, TECH_CLOSED]
                        required: true
                      - id: attr-planned-qty
                        name: 计划数量
                        nameEn: planned_qty
                        dataType: reference
                        valueObjectRef: vo-quantity
                        required: true
                      - id: attr-cost-price
                        name: 成本价
                        nameEn: cost_price
                        dataType: decimal
                        sensitive: true
                    relations:
                      - id: rel-po-bom
                        name: 引用BOM
                        sourceObjectTypeId: production-order
                        targetObjectTypeId: bom
                        cardinality: "N:1"
                        relationKind: reference
                  - id: operation
                    name: 工序
                    nameEn: Operation
                    kind: entity
                    aggregateRootId: production-order
                    properties:
                      - id: attr-op-number
                        name: 工序号
                        nameEn: op_number
                        dataType: integer
                        required: true
                  - id: material
                    name: 物料
                    nameEn: Material
                    kind: aggregate_root
                    properties:
                      - id: attr-material-code
                        name: 物料编码
                        nameEn: material_code
                        dataType: string
                        required: true
                  - id: bom
                    name: BOM
                    nameEn: BillOfMaterial
                    kind: aggregate_root
                  - id: routing
                    name: 工艺路线
                    nameEn: Routing
                    kind: aggregate_root
                stateMachines:
                  - id: sm-production-order
                    name: 生产订单生命周期
                    objectTypeId: production-order
                    statusField: status
                    states:
                      - { name: 创建, code: CREATED, isInitial: true }
                      - { name: 已下达, code: RELEASED }
                      - { name: 执行中, code: IN_PROGRESS }
                      - { name: 已报工, code: REPORTED }
                      - { name: 已产出, code: PRODUCED }
                      - { name: 技术关闭, code: TECH_CLOSED, isFinal: true }
                    transitions:
                      - { name: 下达, from: CREATED, to: RELEASED, trigger: manual, actionId: action-release-order }
              behavior:
                actions:
                  - id: action-create-order
                    name: 创建生产订单
                    nameEn: CreateProductionOrder
                    aggregateRootId: production-order
                    parameters:
                      - { name: 物料编码, nameEn: material_code, dataType: string, required: true }
                    publishesEventIds:
                      - evt-order-created
                  - id: action-release-order
                    name: 生产订单下达
                    nameEn: ReleaseProductionOrder
                    aggregateRootId: production-order
                    businessScenarioIds:
                      - scenario-mts
                      - scenario-mto
                    parameters:
                      - { name: 生产订单号, nameEn: order_id, dataType: string, required: true }
                    preRuleIds:
                      - rule-kitting
                      - rule-routing-valid
                    allowedStateFrom: [CREATED]
                    publishesEventIds:
                      - evt-order-released
                    mcpToolName: execute_production_order_release
                  - id: action-reserve-material
                    name: 物料预留
                    nameEn: ReserveMaterial
                    aggregateRootId: material
                    description: 事件处理器示例
                rules:
                  - id: rule-kitting
                    name: 物料齐套校验
                    type: precondition
                    expression:
                      type: cross_entity
                      description: BOM 中所有物料库存余量 ≥ 工单需求量
                    errorMessage: 物料未齐套，无法下达工单
                  - id: rule-routing-valid
                    name: 工艺路线校验
                    type: precondition
                    expression:
                      type: expression
                      description: 成品必须有且仅有一条有效工艺路线，工序无环
                    errorMessage: 工艺路线无效
                metrics:
                  - id: metric-otd
                    name: 准时完工率
                    formula: on_time_completed / total_completed * 100
                    dataSourceRef: evt-order-tech-closed
                    period: month
                transactionBoundaries:
                  - id: tx-release-order
                    actionId: action-release-order
                    description: 更新状态、预留物料、生成工序任务须原子完成
                    operations:
                      - { type: update_state, field: status, value: RELEASED }
                      - { type: reserve_material }
                      - { type: create_operations }
              events:
                domainEvents:
                  - id: evt-order-created
                    name: 生产订单已创建
                    nameEn: ProductionOrderCreated
                    aggregateRootId: production-order
                    triggerActionId: action-create-order
                    payloadSchema:
                      required: [event_id, timestamp, order_id, status]
                  - id: evt-order-released
                    name: 生产订单已下达
                    nameEn: ProductionOrderReleased
                    aggregateRootId: production-order
                    triggerActionId: action-release-order
                    payloadSchema:
                      required: [event_id, timestamp, order_id, order_status]
                  - id: evt-order-tech-closed
                    name: 生产订单技术关闭
                    nameEn: ProductionOrderTechnicallyClosed
                    aggregateRootId: production-order
                routes:
                  - id: route-order-released
                    eventId: evt-order-released
                    targets:
                      - { boundedContext: 物料管理, system: WMS }
                handlers:
                  - id: handler-reserve-material
                    routeId: route-order-released
                    targetBoundedContext: 物料管理
                    actionId: action-reserve-material
                    businessScenarioIds: [scenario-mts]
              governance:
                roles:
                  - id: role-planner
                    name: 生产计划员
                    permissions:
                      - objectTypeId: production-order
                        ops: [READ, EXECUTE]
                        denyActionIds: []
                  - id: role-cost-accountant
                    name: 成本会计
                    permissions:
                      - objectTypeId: production-order
                        ops: [READ]
                fieldPermissions:
                  - objectTypeId: production-order
                    propertyNameEn: cost_price
                    allowedRoleIds: [role-cost-accountant]
                agentPolicies:
                  - id: sandbox-prod-planner
                    manifestVersion: "0.1.0"
                    roleId: role-planner
                    allowedMcpTools:
                      - query_ontology
                      - execute_production_order_release
                    allowedAggregateRootIds:
                      - production-order
                    allowedActionIds:
                      - action-create-order
                      - action-release-order
                    rateLimit:
                      maxCallsPerSecond: 10
                    defaultDeny: true
              dataSources:
                - id: ds-sap-po
                  name: SAP 生产订单 OData
                  type: api
                  boundObjectTypeId: production-order
                  api:
                    baseUrl: https://sap.example/odata/s4
                    entitySet: ProductionOrders
                    authSecretRef: secret/sap-oauth-prod
            """;

    // ==================== 解析测试 ====================

    @Test
    @DisplayName("TC-1: 完整 YAML 解析 → 元数据正确")
    void shouldParseMetadata() {
        YamlManifest m = parser.parse(MANIFEST_YAML);

        assertThat(m.getApiVersion()).isEqualTo("ontology.platform/v1");
        assertThat(m.getKind()).isEqualTo("OntologyManifest");
        assertThat(m.getMetadata().getId()).isEqualTo("manufacturing-ontology");
        assertThat(m.getMetadata().getName()).isEqualTo("生产制造本体");
        assertThat(m.getMetadata().getDisplayName()).isEqualTo("生产制造本体");
        assertThat(m.getMetadata().getBoundedContext()).isEqualTo("生产制造");
        assertThat(m.getMetadata().getDomainTags()).containsExactly("生产制造");
        assertThat(m.getMetadata().getStatus()).isEqualTo("draft");
    }

    @Test
    @DisplayName("TC-2: ObjectType 解析 → 5 个 ObjectType + properties/relations")
    void shouldParseObjectTypes() {
        YamlManifest m = parser.parse(MANIFEST_YAML);
        var objectTypes = m.getSpec().getSemantic().getObjectTypes();

        assertThat(objectTypes).hasSize(5);

        var po = objectTypes.stream()
                .filter(o -> "production-order".equals(o.getId()))
                .findFirst().orElseThrow();

        assertThat(po.getName()).isEqualTo("生产订单");
        assertThat(po.getKind()).isEqualTo("aggregate_root");
        assertThat(po.getProperties()).hasSize(5);
        assertThat(po.getRelations()).hasSize(1);

        // 检查属性
        assertThat(po.getProperties()).anyMatch(p -> "attr-order-id".equals(p.getId()));
        assertThat(po.getProperties()).anyMatch(p -> "attr-cost-price".equals(p.getId()) && Boolean.TRUE.equals(p.getSensitive()));
        assertThat(po.getProperties()).anyMatch(p -> "attr-order-type".equals(p.getId()) && p.getEnumValues() != null && p.getEnumValues().size() == 2);

        // 检查关系
        assertThat(po.getRelations().get(0).getTargetObjectTypeId()).isEqualTo("bom");
    }

    @Test
    @DisplayName("TC-3: 业务场景解析 → 2 scenarios")
    void shouldParseBusinessScenarios() {
        YamlManifest m = parser.parse(MANIFEST_YAML);
        var scenarios = m.getSpec().getSemantic().getBusinessScenarios();

        assertThat(scenarios).hasSize(2);
        assertThat(scenarios.get(0).getId()).isEqualTo("scenario-mts");
        assertThat(scenarios.get(1).getId()).isEqualTo("scenario-mto");
    }

    @Test
    @DisplayName("TC-4: 值对象解析 → 1 ValueObject with 2 properties")
    void shouldParseValueObjects() {
        YamlManifest m = parser.parse(MANIFEST_YAML);
        var vos = m.getSpec().getSemantic().getValueObjects();

        assertThat(vos).hasSize(1);
        assertThat(vos.get(0).getId()).isEqualTo("vo-quantity");
        assertThat(vos.get(0).getProperties()).hasSize(2);
    }

    @Test
    @DisplayName("TC-5: 状态机解析 → 1 StateMachine with 6 states + 1 transition")
    void shouldParseStateMachines() {
        YamlManifest m = parser.parse(MANIFEST_YAML);
        var sms = m.getSpec().getSemantic().getStateMachines();

        assertThat(sms).hasSize(1);
        assertThat(sms.get(0).getId()).isEqualTo("sm-production-order");
        assertThat(sms.get(0).getStates()).hasSize(6);
        assertThat(sms.get(0).getTransitions()).hasSize(1);

        // 检查初始状态
        assertThat(sms.get(0).getStates())
                .anyMatch(s -> "CREATED".equals(s.getCode()) && Boolean.TRUE.equals(s.getIsInitial()));
    }

    @Test
    @DisplayName("TC-6: 行为解析 → 3 Actions + 2 Rules + 1 Metric + 1 TransactionBoundary")
    void shouldParseBehavior() {
        YamlManifest m = parser.parse(MANIFEST_YAML);
        var behavior = m.getSpec().getBehavior();

        assertThat(behavior.getActions()).hasSize(3);
        assertThat(behavior.getRules()).hasSize(2);
        assertThat(behavior.getMetrics()).hasSize(1);
        assertThat(behavior.getTransactionBoundaries()).hasSize(1);

        // 检查动作详情
        var releaseAction = behavior.getActions().stream()
                .filter(a -> "action-release-order".equals(a.getId()))
                .findFirst().orElseThrow();
        assertThat(releaseAction.getPreRuleIds()).contains("rule-kitting", "rule-routing-valid");
        assertThat(releaseAction.getMcpToolName()).isEqualTo("execute_production_order_release");
    }

    @Test
    @DisplayName("TC-7: 事件解析 → 3 DomainEvents + 1 Route + 1 Handler")
    void shouldParseEvents() {
        YamlManifest m = parser.parse(MANIFEST_YAML);
        var events = m.getSpec().getEvents();

        assertThat(events.getDomainEvents()).hasSize(3);
        assertThat(events.getRoutes()).hasSize(1);
        assertThat(events.getHandlers()).hasSize(1);
    }

    @Test
    @DisplayName("TC-8: 全量转换 → 实体非空校验")
    void shouldConvertToEntities() {
        YamlManifest manifest = parser.parse(MANIFEST_YAML);
        YamlImportResult result = converter.convert(manifest);

        assertThat(result.getOntology()).isNotNull();
        assertThat(result.getOntology().getDisplayName()).isEqualTo("生产制造本体");
        assertThat(result.getObjectTypes()).isNotEmpty();

        // objectTypes: 5 explicit + 2 scenarios + 1 valueObject = 8
        assertThat(result.getObjectTypes().size()).isGreaterThanOrEqualTo(5);

        assertThat(result.getStateMachines()).hasSize(1);
        assertThat(result.getActions()).hasSize(3);
        assertThat(result.getDomainEvents()).hasSize(3);
        assertThat(result.getTotalEntities()).isGreaterThan(10);
    }

    @Test
    @DisplayName("空 YAML → 异常")
    void shouldThrowOnEmptyYaml() {
        assertThatThrownBy(() -> parser.parse(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> parser.parse("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("非法 YAML → 异常")
    void shouldThrowOnInvalidYaml() {
        assertThatThrownBy(() -> parser.parse("not: [valid: yaml: broken"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Governance 解析 → 2 Roles + 1 FieldPermission + 1 AgentPolicy")
    void shouldParseGovernance() {
        YamlManifest m = parser.parse(MANIFEST_YAML);
        var gov = m.getSpec().getGovernance();

        assertThat(gov.getRoles()).hasSize(2);
        assertThat(gov.getFieldPermissions()).hasSize(1);
        assertThat(gov.getAgentPolicies()).hasSize(1);

        assertThat(gov.getRoles().get(0).getId()).isEqualTo("role-planner");
        assertThat(gov.getAgentPolicies().get(0).getRateLimit().getMaxCallsPerSecond()).isEqualTo(10);
    }

    @Test
    @DisplayName("DataSources 解析 → 1 API DataSource")
    void shouldParseDataSources() {
        YamlManifest m = parser.parse(MANIFEST_YAML);
        var ds = m.getSpec().getDataSources();

        assertThat(ds).hasSize(1);
        assertThat(ds.get(0).getId()).isEqualTo("ds-sap-po");
        assertThat(ds.get(0).getApi().getBaseUrl()).contains("sap.example");
    }

    @Test
    @DisplayName("最小 YAML → 无附加字段时也能解析")
    void shouldParseMinimalYaml() {
        String minimal = """
                apiVersion: ontology.platform/v1
                kind: OntologyManifest
                metadata:
                  id: minimal-test
                  name: 最小测试
                """;
        YamlManifest m = parser.parse(minimal);
        assertThat(m.getMetadata().getId()).isEqualTo("minimal-test");
        assertThat(m.getSpec()).isNull(); // 无 spec 时允许 null
    }
}
