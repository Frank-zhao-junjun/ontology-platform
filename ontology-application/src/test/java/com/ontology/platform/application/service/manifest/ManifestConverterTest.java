package com.ontology.platform.application.service.manifest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.vo.manifest.ManifestDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ManifestConverterTest {

    private ManifestConverter converter;

    @BeforeEach
    void setUp() {
        converter = new ManifestConverter(new ObjectMapper());
    }

    @Test
    @DisplayName("TC-1: 完整 Manifest → 对象类型/动作/事件/EPC 全部映射")
    void shouldConvertFullManifest() {
        String json = """
                {
                  "apiVersion": "ontology.platform/v1",
                  "kind": "OntologyManifest",
                  "metadata": {
                    "id": "manifest-001",
                    "name": "生产制造",
                    "version": "0.1.0",
                    "description": "生产制造领域本体"
                  },
                  "spec": {
                    "semantic": {
                      "objectTypes": [
                        {"id": "VD-001", "name": "生产制造", "kind": "aggregate_root"},
                        {"id": "ENT-001", "name": "生产订单", "kind": "entity"}
                      ]
                    },
                    "behavior": {
                      "actions": [
                        {"id": "ACT-001", "name": "下达", "nameEn": "release"}
                      ],
                      "rules": [
                        {"id": "RUL-001", "name": "库存校验", "expression": "stock > 0"}
                      ]
                    },
                    "events": {
                      "domainEvents": [
                        {"id": "EVT-001", "nameEn": "order_created"}
                      ]
                    },
                    "process": {
                      "orchestrations": [
                        {
                          "id": "EPC-001",
                          "name": "订单流程",
                          "steps": [
                            {"id": "s1", "type": "action", "actionId": "ACT-001"}
                          ]
                        }
                      ]
                    },
                    "dataSources": [
                      {"id": "DS-001", "name": "SAP", "sourceType": "api"}
                    ]
                  }
                }
                """;

        ManifestDocument doc = converter.convert(json);

        // metadata
        assertThat(doc.getMetadata()).isNotNull();
        assertThat(doc.getMetadata().getName()).isEqualTo("生产制造");

        // semantic → objectTypes
        assertThat(doc.getObjectTypes()).hasSize(2);
        assertThat(doc.getObjectTypes().get(0).getId()).isEqualTo("VD-001");

        // behavior → actions
        assertThat(doc.getActions()).hasSize(1);
        assertThat(doc.getActions().get(0).getName()).isEqualTo("下达");

        // behavior → rules
        assertThat(doc.getRules()).hasSize(1);
        assertThat(doc.getRules().get(0).getName()).isEqualTo("库存校验");

        // events
        assertThat(doc.getEvents()).hasSize(1);
        assertThat(doc.getEvents().get(0).getNameEn()).isEqualTo("order_created");

        // epc
        assertThat(doc.getSpec().getEpc()).hasSize(1);
        assertThat(doc.getSpec().getEpc().get(0).getFlowName()).isEqualTo("订单流程");
        assertThat(doc.getSpec().getEpc().get(0).getSteps()).hasSize(1);
    }

    @Test
    @DisplayName("TC-2: 空字段 → 空列表")
    void shouldHandleEmptyFields() {
        String json = """
                {
                  "apiVersion": "ontology.platform/v1",
                  "kind": "OntologyManifest",
                  "metadata": {"id": "empty", "name": "空Manifest", "version": "1.0"},
                  "spec": {}
                }
                """;

        ManifestDocument doc = converter.convert(json);
        assertThat(doc.getObjectTypes()).isEmpty();
        assertThat(doc.getActions()).isEmpty();
        assertThat(doc.getEvents()).isEmpty();
        assertThat(doc.getRules()).isEmpty();
        assertThat(doc.getStateMachines()).isEmpty();
    }

    @Test
    @DisplayName("TC-3: 非法 JSON → 异常")
    void shouldThrowOnInvalidJson() {
        assertThatThrownBy(() -> converter.convert("{not valid json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JSON");
    }

    @Test
    @DisplayName("StateMachine 在 semantic 中→ 映射到 behavior.stateMachines")
    void shouldConvertStateMachinesFromSemantic() {
        String json = """
                {
                  "apiVersion": "ontology.platform/v1",
                  "kind": "OntologyManifest",
                  "metadata": {"id": "sm-test", "name": "状态机测试", "version": "1.0"},
                  "spec": {
                    "semantic": {
                      "objectTypes": [],
                      "stateMachines": [
                        {
                          "id": "SM-001",
                          "name": "订单状态机",
                          "states": [
                            {"name": "draft", "isInitial": true, "isFinal": false},
                            {"name": "confirmed", "isInitial": false, "isFinal": true}
                          ],
                          "transitions": [
                            {"from": "draft", "to": "confirmed", "trigger": "确认"}
                          ]
                        }
                      ]
                    }
                  }
                }
                """;

        ManifestDocument doc = converter.convert(json);
        assertThat(doc.getStateMachines()).hasSize(1);
        assertThat(doc.getStateMachines().get(0).getName()).isEqualTo("订单状态机");
        assertThat(doc.getStateMachines().get(0).getStates()).hasSize(2);
        assertThat(doc.getStateMachines().get(0).getTransitions()).hasSize(1);
    }
}
