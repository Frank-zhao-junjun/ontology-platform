package com.ontology.platform.infrastructure.imports;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.dto.imports.YamlManifest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link ManifestUpcasterV1ToV2}.
 *
 * <p>Verifies v1 YAML manifest → v2 OntologyExchangeDocument conversion,
 * using the golden JSON fixture as expected reference.</p>
 */
class ManifestUpcasterV1ToV2Test {

    private final YamlManifestParser parser = new YamlManifestParser();
    private final ManifestUpcasterV1ToV2 upcaster = new ManifestUpcasterV1ToV2();

    private static final String V1_MANIFEST = """
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
              events:
                domainEvents:
                  - id: evt-order-created
                    name: 生产订单已创建
                    nameEn: ProductionOrderCreated
                    aggregateRootId: production-order
                    triggerActionId: action-create-order
                  - id: evt-order-released
                    name: 生产订单已下达
                    nameEn: ProductionOrderReleased
                    aggregateRootId: production-order
                    triggerActionId: action-release-order
                  - id: evt-order-tech-closed
                    name: 生产订单技术关闭
                    nameEn: ProductionOrderTechnicallyClosed
                    aggregateRootId: production-order
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
                    roleId: role-planner
                    allowedMcpTools:
                      - query_ontology
                      - execute_production_order_release
                    allowedAggregateRootIds:
                      - production-order
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

    @Test
    @DisplayName("v1 YAML → v2 Envelope 元数据正确")
    void upcast_metadata() {
        YamlManifest v1 = parser.parse(V1_MANIFEST);
        OntologyExchangeDocument v2 = upcaster.upcast(v1);

        assertThat(v2.getApiVersion()).isEqualTo("ontology.platform/v2");
        assertThat(v2.getKind()).isEqualTo("OntologyExchange");
        assertThat(v2.getMetadata().getId()).isEqualTo("manufacturing-ontology");
        assertThat(v2.getMetadata().getName()).isEqualTo("生产制造本体");
        assertThat(v2.getMetadata().getStatus()).isEqualTo("draft");
    }

    @Test
    @DisplayName("v1 → v2: 6 个 Entity (5 objectTypes + 1 valueObject)")
    void upcast_entities() {
        YamlManifest v1 = parser.parse(V1_MANIFEST);
        OntologyExchangeDocument v2 = upcaster.upcast(v1);

        var entities = v2.getSpec().getProject().getDataModel().getEntities();
        assertThat(entities).hasSize(6);

        // production-order: aggregate_root
        var po = entities.stream().filter(e -> "production-order".equals(e.getId())).findFirst().orElseThrow();
        assertThat(po.getEntityRole()).isEqualTo("aggregate_root");
        assertThat(po.getName()).isEqualTo("生产订单");

        // operation: child_entity with parentAggregateId
        var op = entities.stream().filter(e -> "operation".equals(e.getId())).findFirst().orElseThrow();
        assertThat(op.getEntityRole()).isEqualTo("child_entity");
        assertThat(op.getParentAggregateId()).isEqualTo("production-order");

        // vo-quantity: from valueObjects, entityRole=child_entity
        var vo = entities.stream().filter(e -> "vo-quantity".equals(e.getId())).findFirst().orElseThrow();
        assertThat(vo.getEntityRole()).isEqualTo("child_entity");
        assertThat(vo.getAttributes()).hasSize(2);
    }

    @Test
    @DisplayName("v1 → v2: Entity attributes 正确映射")
    void upcast_attributes() {
        YamlManifest v1 = parser.parse(V1_MANIFEST);
        OntologyExchangeDocument v2 = upcaster.upcast(v1);

        var po = v2.getSpec().getProject().getDataModel().getEntities().stream()
                .filter(e -> "production-order".equals(e.getId())).findFirst().orElseThrow();

        assertThat(po.getAttributes()).hasSize(5);
        assertThat(po.getAttributes()).anyMatch(a -> "attr-order-id".equals(a.getId()) && "string".equals(a.getDataType()));
        assertThat(po.getAttributes()).anyMatch(a -> "attr-cost-price".equals(a.getId()) && "decimal".equals(a.getDataType()));
    }

    @Test
    @DisplayName("v1 → v2: Relations 映射 (type=many_to_one)")
    void upcast_relations() {
        YamlManifest v1 = parser.parse(V1_MANIFEST);
        OntologyExchangeDocument v2 = upcaster.upcast(v1);

        var po = v2.getSpec().getProject().getDataModel().getEntities().stream()
                .filter(e -> "production-order".equals(e.getId())).findFirst().orElseThrow();

        assertThat(po.getRelations()).hasSize(1);
        assertThat(po.getRelations().get(0).getTargetEntity()).isEqualTo("bom");
        assertThat(po.getRelations().get(0).getType()).isEqualTo("many_to_one");
    }

    @Test
    @DisplayName("v1 → v2: StateMachine + Actions")
    void upcast_behavior() {
        YamlManifest v1 = parser.parse(V1_MANIFEST);
        OntologyExchangeDocument v2 = upcaster.upcast(v1);

        var bm = v2.getSpec().getProject().getBehaviorModel();
        assertThat(bm).isNotNull();
        assertThat(bm.getStateMachines()).hasSize(1);
        assertThat(bm.getActions()).hasSize(3);
        assertThat(bm.getStateMachines().get(0).getEntity()).isEqualTo("production-order");
    }

    @Test
    @DisplayName("v1 → v2: Rules + Events + Governance + DataSources")
    void upcast_otherSections() {
        YamlManifest v1 = parser.parse(V1_MANIFEST);
        OntologyExchangeDocument v2 = upcaster.upcast(v1);

        var project = v2.getSpec().getProject();

        // Rules
        assertThat(project.getRuleModel().getRules()).hasSize(2);

        // Events
        assertThat(project.getEventModel().getEvents()).hasSize(3);

        // Governance
        assertThat(project.getGovernanceModel().getRoles()).hasSize(2);
        assertThat(project.getGovernanceModel().getAgentPolicies()).hasSize(1);

        // DataSources
        assertThat(project.getDataSourcesModel().getSources()).hasSize(1);
        assertThat(project.getDataSourcesModel().getSources().get(0).getId()).isEqualTo("ds-sap-po");
    }

    @Test
    @DisplayName("v1 YAML 空 → upcaster 返回 null")
    void upcast_nullInput() {
        assertThat(upcaster.upcast(null)).isNull();
    }
}
