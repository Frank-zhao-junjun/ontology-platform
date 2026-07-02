package com.ontology.platform.infrastructure.imports;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Project1JsonToExchangeConverter} focusing on
 * {@link com.ontology.platform.domain.dto.imports.OntologyExchangeDocument.EpcProfile#profileData}
 * field mapping.
 *
 * <p>Verifies both raw format and v1 manifest format inputs produce correct
 * EpcProfile.profileData values (present, null, or absent) and that chains
 * continue to work correctly alongside profiles.</p>
 */
class Project1JsonToExchangeConverterTest {

    private Project1JsonToExchangeConverter converter;

    @BeforeEach
    void setUp() {
        converter = new Project1JsonToExchangeConverter(new ObjectMapper());
    }

    // ==================== Raw format — profileData present ====================

    @Test
    @DisplayName("Raw format: EpcProfile with profileData is mapped correctly")
    void rawFormat_withProfileData() {
        String json = """
                {
                  "version": "1.0",
                  "project": { "id": "proj-1", "name": "Test Project" },
                  "epc": {
                    "chains": [],
                    "profiles": [
                      {
                        "id": "prof-1",
                        "chainId": "chain-1",
                        "profileData": "{\\"key\\":\\"value\\",\\"enabled\\":true}",
                        "profileVersion": "1.0"
                      }
                    ]
                  }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var epcModel = doc.getSpec().getProject().getEpcModel();
        assertThat(epcModel).isNotNull();
        assertThat(epcModel.getProfiles()).hasSize(1);

        var profile = epcModel.getProfiles().get(0);
        assertThat(profile.getId()).isEqualTo("prof-1");
        assertThat(profile.getChainId()).isEqualTo("chain-1");
        assertThat(profile.getProfileData()).isEqualTo("{\"key\":\"value\",\"enabled\":true}");
        assertThat(profile.getProfileVersion()).isEqualTo("1.0");
    }

    @Test
    @DisplayName("Raw format: EpcProfile without profileData has null profileData")
    void rawFormat_withoutProfileData() {
        String json = """
                {
                  "version": "1.0",
                  "project": { "id": "proj-1", "name": "Test Project" },
                  "epc": {
                    "chains": [],
                    "profiles": [
                      {
                        "id": "prof-2",
                        "chainId": "chain-1",
                        "profileVersion": "1.0"
                      }
                    ]
                  }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var epcModel = doc.getSpec().getProject().getEpcModel();
        assertThat(epcModel.getProfiles()).hasSize(1);

        var profile = epcModel.getProfiles().get(0);
        assertThat(profile.getId()).isEqualTo("prof-2");
        assertThat(profile.getProfileData()).isNull();
        assertThat(profile.getProfileVersion()).isEqualTo("1.0");
    }

    @Test
    @DisplayName("Raw format: EpcProfile with empty profileData has null profileData")
    void rawFormat_withEmptyProfileData() {
        String json = """
                {
                  "version": "1.0",
                  "project": { "id": "proj-1", "name": "Test Project" },
                  "epc": {
                    "chains": [],
                    "profiles": [
                      {
                        "id": "prof-3",
                        "chainId": "chain-1",
                        "profileData": "",
                        "profileVersion": "1.0"
                      }
                    ]
                  }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var epcModel = doc.getSpec().getProject().getEpcModel();
        assertThat(epcModel.getProfiles()).hasSize(1);

        var profile = epcModel.getProfiles().get(0);
        assertThat(profile.getId()).isEqualTo("prof-3");
        // pathStr returns null for blank strings
        assertThat(profile.getProfileData()).isNull();
    }

    @Test
    @DisplayName("Raw format: EpcProfile with null profileData has null profileData")
    void rawFormat_withNullProfileData() {
        String json = """
                {
                  "version": "1.0",
                  "project": { "id": "proj-1", "name": "Test Project" },
                  "epc": {
                    "chains": [],
                    "profiles": [
                      {
                        "id": "prof-4",
                        "chainId": "chain-1",
                        "profileData": null,
                        "profileVersion": "1.0"
                      }
                    ]
                  }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var epcModel = doc.getSpec().getProject().getEpcModel();
        assertThat(epcModel.getProfiles()).hasSize(1);

        var profile = epcModel.getProfiles().get(0);
        assertThat(profile.getId()).isEqualTo("prof-4");
        assertThat(profile.getProfileData()).isNull();
    }

    // ==================== Raw format — no epc / no profiles ====================

    @Test
    @DisplayName("Raw format: no epc section returns empty EpcModel with no profiles")
    void rawFormat_noEpcSection() {
        String json = """
                {
                  "version": "1.0",
                  "project": { "id": "proj-1", "name": "Test Project" }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var epcModel = doc.getSpec().getProject().getEpcModel();
        assertThat(epcModel).isNotNull();
        assertThat(epcModel.getProfiles()).isEmpty();
        assertThat(epcModel.getChains()).isEmpty();
    }

    @Test
    @DisplayName("Raw format: epc section with no profiles returns empty profiles list")
    void rawFormat_epcWithoutProfiles() {
        String json = """
                {
                  "version": "1.0",
                  "project": { "id": "proj-1", "name": "Test Project" },
                  "epc": {
                    "chains": []
                  }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var epcModel = doc.getSpec().getProject().getEpcModel();
        assertThat(epcModel.getProfiles()).isEmpty();
    }

    // ==================== Raw format — chains alongside profiles ====================

    @Test
    @DisplayName("Raw format: chains and profiles both work correctly together")
    void rawFormat_chainsAndProfiles() {
        String json = """
                {
                  "version": "1.0",
                  "project": { "id": "proj-1", "name": "Test Project" },
                  "epc": {
                    "chains": [
                      {
                        "id": "chain-1",
                        "name": "Test Chain",
                        "nodes": [
                          { "id": "node-1", "nodeType": "event", "name": "Start", "sortOrder": 1 }
                        ],
                        "edges": [
                          { "id": "edge-1", "sourceNodeId": "node-1", "targetNodeId": "node-2", "edgeType": "sequence" }
                        ]
                      }
                    ],
                    "profiles": [
                      {
                        "id": "prof-1",
                        "chainId": "chain-1",
                        "profileData": "{\\"profile\\":true}",
                        "profileVersion": "1.0"
                      }
                    ]
                  }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var epcModel = doc.getSpec().getProject().getEpcModel();

        // Verify chains
        assertThat(epcModel.getChains()).hasSize(1);
        var chain = epcModel.getChains().get(0);
        assertThat(chain.getId()).isEqualTo("chain-1");
        assertThat(chain.getName()).isEqualTo("Test Chain");
        assertThat(chain.getNodes()).hasSize(1);
        assertThat(chain.getNodes().get(0).getId()).isEqualTo("node-1");
        assertThat(chain.getEdges()).hasSize(1);

        // Verify profiles
        assertThat(epcModel.getProfiles()).hasSize(1);
        var profile = epcModel.getProfiles().get(0);
        assertThat(profile.getId()).isEqualTo("prof-1");
        assertThat(profile.getChainId()).isEqualTo("chain-1");
        assertThat(profile.getProfileData()).isEqualTo("{\"profile\":true}");
        assertThat(profile.getProfileVersion()).isEqualTo("1.0");
    }

    // ==================== V1 Manifest format — profileData present ====================

    @Test
    @DisplayName("V1 Manifest: EpcProfile with profileData is mapped correctly")
    void v1Manifest_withProfileData() {
        String json = """
                {
                  "apiVersion": "ontology.platform/v1",
                  "kind": "OntologyManifest",
                  "metadata": {
                    "id": "manifest-1",
                    "name": "Test Manifest"
                  },
                  "spec": {
                    "epc": {
                      "chains": [],
                      "profiles": [
                        {
                          "id": "v1-prof-1",
                          "chainId": "v1-chain-1",
                          "profileData": "{\\"version\\":2,\\"active\\":true}",
                          "profileVersion": "2.0"
                        }
                      ]
                    }
                  }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var epcModel = doc.getSpec().getProject().getEpcModel();
        assertThat(epcModel).isNotNull();
        assertThat(epcModel.getProfiles()).hasSize(1);

        var profile = epcModel.getProfiles().get(0);
        assertThat(profile.getId()).isEqualTo("v1-prof-1");
        assertThat(profile.getChainId()).isEqualTo("v1-chain-1");
        assertThat(profile.getProfileData()).isEqualTo("{\"version\":2,\"active\":true}");
        assertThat(profile.getProfileVersion()).isEqualTo("2.0");
    }

    @Test
    @DisplayName("V1 Manifest: EpcProfile without profileData has null profileData")
    void v1Manifest_withoutProfileData() {
        String json = """
                {
                  "apiVersion": "ontology.platform/v1",
                  "kind": "OntologyManifest",
                  "metadata": {
                    "id": "manifest-2",
                    "name": "Test Manifest"
                  },
                  "spec": {
                    "epc": {
                      "chains": [],
                      "profiles": [
                        {
                          "id": "v1-prof-2",
                          "chainId": "v1-chain-1",
                          "profileVersion": "1.0"
                        }
                      ]
                    }
                  }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var epcModel = doc.getSpec().getProject().getEpcModel();
        assertThat(epcModel.getProfiles()).hasSize(1);

        var profile = epcModel.getProfiles().get(0);
        assertThat(profile.getId()).isEqualTo("v1-prof-2");
        assertThat(profile.getProfileData()).isNull();
        assertThat(profile.getProfileVersion()).isEqualTo("1.0");
    }

    // ==================== V1 Manifest format — no epc / no profiles ====================

    @Test
    @DisplayName("V1 Manifest: no epc section returns empty EpcModel")
    void v1Manifest_noEpcSection() {
        String json = """
                {
                  "apiVersion": "ontology.platform/v1",
                  "kind": "OntologyManifest",
                  "metadata": {
                    "id": "manifest-3",
                    "name": "Test Manifest"
                  },
                  "spec": {
                    "semantic": {}
                  }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var epcModel = doc.getSpec().getProject().getEpcModel();
        assertThat(epcModel).isNotNull();
        assertThat(epcModel.getProfiles()).isEmpty();
        assertThat(epcModel.getChains()).isEmpty();
    }

    @Test
    @DisplayName("V1 Manifest: epc with no profiles returns empty profiles list")
    void v1Manifest_epcWithoutProfiles() {
        String json = """
                {
                  "apiVersion": "ontology.platform/v1",
                  "kind": "OntologyManifest",
                  "metadata": {
                    "id": "manifest-4",
                    "name": "Test Manifest"
                  },
                  "spec": {
                    "epc": {
                      "chains": [
                        {
                          "id": "v1-chain-1",
                          "name": "V1 Chain",
                          "nodes": [],
                          "edges": []
                        }
                      ]
                    }
                  }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var epcModel = doc.getSpec().getProject().getEpcModel();
        assertThat(epcModel.getProfiles()).isEmpty();
        assertThat(epcModel.getChains()).hasSize(1);
        assertThat(epcModel.getChains().get(0).getId()).isEqualTo("v1-chain-1");
    }

    // ==================== V1 Manifest — chains alongside profiles ====================

    @Test
    @DisplayName("V1 Manifest: chains and profiles both work correctly together")
    void v1Manifest_chainsAndProfiles() {
        String json = """
                {
                  "apiVersion": "ontology.platform/v1",
                  "kind": "OntologyManifest",
                  "metadata": {
                    "id": "manifest-5",
                    "name": "Test Manifest"
                  },
                  "spec": {
                    "epc": {
                      "chains": [
                        {
                          "id": "v1-chain-1",
                          "name": "V1 Chain",
                          "nodes": [
                            { "id": "n1", "nodeType": "function", "name": "Check", "sortOrder": 1 }
                          ],
                          "edges": [
                            { "id": "e1", "sourceNodeId": "n1", "targetNodeId": "n2", "edgeType": "flow" }
                          ]
                        }
                      ],
                      "profiles": [
                        {
                          "id": "v1-prof-1",
                          "chainId": "v1-chain-1",
                          "profileData": "{\\"threshold\\":100,\\"unit\\":\\"ms\\"}",
                          "profileVersion": "3.0"
                        }
                      ]
                    }
                  }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var epcModel = doc.getSpec().getProject().getEpcModel();

        // Verify chains
        assertThat(epcModel.getChains()).hasSize(1);
        var chain = epcModel.getChains().get(0);
        assertThat(chain.getId()).isEqualTo("v1-chain-1");
        assertThat(chain.getName()).isEqualTo("V1 Chain");
        assertThat(chain.getNodes()).hasSize(1);
        assertThat(chain.getNodes().get(0).getName()).isEqualTo("Check");
        assertThat(chain.getEdges()).hasSize(1);

        // Verify profiles
        assertThat(epcModel.getProfiles()).hasSize(1);
        var profile = epcModel.getProfiles().get(0);
        assertThat(profile.getId()).isEqualTo("v1-prof-1");
        assertThat(profile.getChainId()).isEqualTo("v1-chain-1");
        assertThat(profile.getProfileData()).isEqualTo("{\"threshold\":100,\"unit\":\"ms\"}");
        assertThat(profile.getProfileVersion()).isEqualTo("3.0");
    }

    // ==================== Entity role mapping (Project1 ↔ Project2) ====================

    @Test
    @DisplayName("normalizeEntityRole: kind=entity maps to child_entity")
    void entityKindMapsToChildEntity() {
        assertThat(Project1JsonToExchangeConverter.normalizeEntityRole("entity")).isEqualTo("child_entity");
    }

    @Test
    @DisplayName("normalizeEntityRole: kind=aggregate_root stays aggregate_root")
    void aggregateRootKindStaysAggregateRoot() {
        assertThat(Project1JsonToExchangeConverter.normalizeEntityRole("aggregate_root")).isEqualTo("aggregate_root");
    }

    @Test
    @DisplayName("normalizeEntityRole: entityRole=child_entity stays child_entity")
    void childEntityRoleStaysChildEntity() {
        assertThat(Project1JsonToExchangeConverter.normalizeEntityRole("child_entity")).isEqualTo("child_entity");
    }

    @Test
    @DisplayName("normalizeEntityRole: null or unknown defaults to aggregate_root")
    void nullOrUnknownRoleDefaultsToAggregateRoot() {
        assertThat(Project1JsonToExchangeConverter.normalizeEntityRole(null)).isEqualTo("aggregate_root");
        assertThat(Project1JsonToExchangeConverter.normalizeEntityRole("unknown")).isEqualTo("aggregate_root");
        assertThat(Project1JsonToExchangeConverter.normalizeEntityRole("  ")).isEqualTo("aggregate_root");
    }

    @Test
    @DisplayName("Raw format: entity kind is converted to child_entity")
    void rawFormat_entityKind_convertedToChildEntity() {
        String json = """
                {
                  "version": "1.0",
                  "project": { "id": "proj-1", "name": "Test Project" },
                  "entities": [
                    { "id": "ent-1", "name": "聚合根", "kind": "aggregate_root" },
                    { "id": "ent-2", "name": "子实体", "kind": "entity" },
                    { "id": "ent-3", "name": "子实体2", "entityRole": "child_entity" }
                  ]
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var entities = doc.getSpec().getProject().getDataModel().getEntities();
        assertThat(entities).hasSize(3);
        assertThat(entities.get(0).getEntityRole()).isEqualTo("aggregate_root");
        assertThat(entities.get(1).getEntityRole()).isEqualTo("child_entity");
        assertThat(entities.get(2).getEntityRole()).isEqualTo("child_entity");
    }

    @Test
    @DisplayName("V1 Manifest: objectType kind=entity is converted to child_entity")
    void v1Manifest_entityKind_convertedToChildEntity() {
        String json = """
                {
                  "apiVersion": "ontology.platform/v1",
                  "kind": "OntologyManifest",
                  "metadata": { "id": "manifest-role", "name": "Role Test" },
                  "spec": {
                    "semantic": {
                      "objectTypes": [
                        { "id": "ot-1", "name": "聚合根", "kind": "aggregate_root" },
                        { "id": "ot-2", "name": "子实体", "kind": "entity" }
                      ]
                    }
                  }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var entities = doc.getSpec().getProject().getDataModel().getEntities();
        assertThat(entities).hasSize(2);
        assertThat(entities.get(0).getEntityRole()).isEqualTo("aggregate_root");
        assertThat(entities.get(1).getEntityRole()).isEqualTo("child_entity");
    }

    // ==================== AgentSemanticLayer / EntityLifecycle (Project1 latest modules) ====================

    @Test
    @DisplayName("Raw format: AgentSemanticLayer JSON is imported into v2 exchange document")
    void rawFormat_agentSemanticLayer_imported() {
        String json = """
                {
                  "version": "1.0",
                  "project": { "id": "proj-asl", "name": "ASL Project" },
                  "agentSemanticLayer": {
                    "intents": [
                      {
                        "id": "int-release-order",
                        "name": "下达生产订单",
                        "category": "production",
                        "actionId": "act-release-order",
                        "priority": 10,
                        "requiresConfirmation": true
                      }
                    ],
                    "businessTerms": [
                      {
                        "id": "term-production-order",
                        "name": "生产订单",
                        "nameEn": "ProductionOrder",
                        "definition": "制造执行的核心单据"
                      }
                    ],
                    "semanticRelations": [
                      {
                        "id": "rel-prod-insp",
                        "sourceTermId": "term-production-order",
                        "targetTermId": "term-quality-inspection",
                        "relationType": "has_inspection"
                      }
                    ],
                    "agentPolicies": [
                      {
                        "id": "ap-prod-manager",
                        "roleId": "role-prod-manager",
                        "defaultDeny": false
                      }
                    ],
                    "errorRecoveries": [
                      {
                        "id": "er-release-fallback",
                        "actionId": "act-release-order",
                        "errorPattern": "ORDER_NOT_FOUND",
                        "recoveryStrategy": "retry_with_new_id",
                        "maxRetries": 3
                      }
                    ],
                    "fieldMappings": [
                      {
                        "id": "fm-order-id",
                        "entityId": "production-order",
                        "fieldNameEn": "orderId",
                        "businessTermId": "term-production-order",
                        "mappingType": "direct"
                      }
                    ]
                  }
                }
                """;

        OntologyExchangeDocument doc = converter.convert(json);

        assertThat(doc).isNotNull();
        var asl = doc.getSpec().getProject().getAgentSemanticLayer();
        assertThat(asl).isNotNull();
        assertThat(asl.getIntents()).hasSize(1);
        assertThat(asl.getIntents().get(0).getId()).isEqualTo("int-release-order");
        assertThat(asl.getIntents().get(0).getPriority()).isEqualTo(10);
        assertThat(asl.getBusinessTerms()).hasSize(1);
        assertThat(asl.getBusinessTerms().get(0).getNameEn()).isEqualTo("ProductionOrder");
        assertThat(asl.getSemanticRelations()).hasSize(1);
        assertThat(asl.getSemanticRelations().get(0).getRelationType()).isEqualTo("has_inspection");
        assertThat(asl.getAgentPolicies()).hasSize(1);
        assertThat(asl.getAgentPolicies().get(0).getRoleId()).isEqualTo("role-prod-manager");
        assertThat(asl.getErrorRecoveries()).hasSize(1);
        assertThat(asl.getErrorRecoveries().get(0).getMaxRetries()).isEqualTo(3);
        assertThat(asl.getFieldMappings()).hasSize(1);
        assertThat(asl.getFieldMappings().get(0).getMappingType()).isEqualTo("direct");
    }

    // ==================== Null / blank input ====================

    @Test
    @DisplayName("Null input returns null")
    void nullInput() {
        assertThat(converter.convert(null)).isNull();
    }

    @Test
    @DisplayName("Blank input returns null")
    void blankInput() {
        assertThat(converter.convert("   ")).isNull();
    }
}
