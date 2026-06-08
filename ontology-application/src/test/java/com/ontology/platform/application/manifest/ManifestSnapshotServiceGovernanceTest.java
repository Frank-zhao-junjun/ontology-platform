package com.ontology.platform.application.manifest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.application.service.BehaviorService;
import com.ontology.platform.application.service.GovernanceService;
import com.ontology.platform.application.service.ModelingService;
import com.ontology.platform.domain.entity.AgentSandbox;
import com.ontology.platform.domain.entity.BoundedContext;
import com.ontology.platform.domain.entity.FieldPermission;
import com.ontology.platform.domain.entity.ObjectPermission;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.Role;
import com.ontology.platform.domain.repository.BoundedContextRepository;
import com.ontology.platform.domain.repository.PublishedManifestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ManifestSnapshotServiceGovernanceTest {

    @Mock private BoundedContextRepository contextRepo;
    @Mock private PublishedManifestRepository manifestRepo;
    @Mock private ModelingService modelingService;
    @Mock private BehaviorService behaviorService;
    @Mock private GovernanceService governanceService;

    private ManifestSnapshotService service;
    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String CTX_ID = "ctx-test";
    private static final String ROLE_ID = "role-1";

    @BeforeEach
    void setUp() {
        service = new ManifestSnapshotService(contextRepo, manifestRepo, modelingService,
                behaviorService, governanceService);
    }

    @Test
    void buildSnapshotShouldIncludeGovernanceSection() throws Exception {
        // given
        BoundedContext ctx = new BoundedContext();
        ctx.setId(CTX_ID);
        ctx.setCode("manufacturing");
        ctx.setName("Manufacturing");
        ctx.setDomainTag("manufacturing");
        when(contextRepo.findById(CTX_ID)).thenReturn(Optional.of(ctx));

        // no ARs, no actions, no events, no rules for simplicity
        when(modelingService.listAggregateRoots(CTX_ID)).thenReturn(List.of());
        when(modelingService.listBusinessScenarios(CTX_ID)).thenReturn(List.of());
        when(modelingService.listObjectTypes(CTX_ID)).thenReturn(List.of());
        when(modelingService.listRelations(CTX_ID)).thenReturn(List.of());
        when(behaviorService.listActions(CTX_ID)).thenReturn(List.of());
        when(behaviorService.listRules(CTX_ID)).thenReturn(List.of());

        // roles
        Role planner = new Role();
        planner.setId(ROLE_ID);
        planner.setCode("planner");
        planner.setName("Planner");
        planner.setDescription("Production planner");
        planner.setContextId(CTX_ID);
        planner.setGlobal(false);
        when(governanceService.listRoles(CTX_ID, null)).thenReturn(List.of(planner));

        // object permissions (G01 AC-3)
        ObjectPermission op = new ObjectPermission();
        op.setObjectTypeId("ot-1");
        op.setPermRead(true);
        op.setPermExecute(true);
        when(governanceService.listObjectPermissions(ROLE_ID)).thenReturn(List.of(op));

        // field permissions (G02 AC-2)
        FieldPermission fp = new FieldPermission();
        fp.setObjectTypeId("ot-1");
        fp.setFieldName("cost_price");
        fp.setVisible(false);
        fp.setEditable(false);
        when(governanceService.listFieldPermissions(ROLE_ID)).thenReturn(List.of(fp));

        // resolve object type code
        ObjectType ot = new ObjectType();
        ot.setCode("ProductionOrder");
        when(modelingService.getObjectType("ot-1")).thenReturn(ot);

        // sandboxes (G04 AC-1)
        AgentSandbox sb = new AgentSandbox();
        sb.setName("Agent-1");
        sb.setAgentRoleId(ROLE_ID);
        sb.setAllowedTools(List.of("resolve_intent", "query_ontology", "execute_action"));
        sb.setAllowedAggregateRoots(List.of());
        sb.setAllowedBehaviors(List.of());
        sb.setMaxOpsPerSecond(10);
        when(governanceService.listSandboxes()).thenReturn(List.of(sb));
        when(governanceService.getRole(ROLE_ID)).thenReturn(planner);

        // when
        String manifest = service.buildSnapshot(CTX_ID, "v1.0.0", "Test snapshot");
        JsonNode root = objectMapper.readTree(manifest);

        // then
        assertThat(root.has("governance")).isTrue();
        JsonNode governance = root.get("governance");

        // roles
        assertThat(governance.has("roles")).isTrue();
        JsonNode roles = governance.get("roles");
        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).get("id").asText()).isEqualTo("planner");
        assertThat(roles.get(0).get("name").asText()).isEqualTo("Planner");
        assertThat(roles.get(0).get("isGlobal").asBoolean()).isFalse();

        // permissions
        JsonNode perms = roles.get(0).get("permissions");
        assertThat(perms).hasSize(1);
        assertThat(perms.get(0).get("objectTypeId").asText()).isEqualTo("ProductionOrder");
        assertThat(perms.get(0).get("ops").toString()).contains("READ");
        assertThat(perms.get(0).get("ops").toString()).contains("EXECUTE");

        // field permissions
        assertThat(governance.has("fieldPermissions")).isTrue();
        JsonNode fieldPerms = governance.get("fieldPermissions");
        assertThat(fieldPerms).hasSize(1);
        assertThat(fieldPerms.get(0).get("propertyNameEn").asText()).isEqualTo("cost_price");
        assertThat(fieldPerms.get(0).get("isVisible").asBoolean()).isFalse();
        assertThat(fieldPerms.get(0).get("roleId").asText()).isEqualTo("planner");

        // agent policies
        assertThat(governance.has("agentPolicies")).isTrue();
        JsonNode policies = governance.get("agentPolicies");
        assertThat(policies).hasSize(1);
        assertThat(policies.get(0).get("id").asText()).isEqualTo("Agent-1");
        assertThat(policies.get(0).get("roleId").asText()).isEqualTo("planner");
        assertThat(policies.get(0).get("defaultDeny").asBoolean()).isTrue();
        assertThat(policies.get(0).get("rateLimit").get("maxCallsPerSecond").asInt()).isEqualTo(10);
        assertThat(policies.get(0).get("allowedMcpTools").toString()).contains("resolve_intent");
    }
}
