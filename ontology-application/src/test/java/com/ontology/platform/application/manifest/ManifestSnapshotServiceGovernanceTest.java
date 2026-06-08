package com.ontology.platform.application.manifest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.ontology.platform.application.service.*;
import com.ontology.platform.domain.entity.*;
import com.ontology.platform.domain.repository.BoundedContextRepository;
import com.ontology.platform.domain.repository.PublishedManifestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

/**
 * 治理层 Manifest 编译测试 (US-G01/G02/G04)。
 * 验证角色权限、字段权限、Agent 沙箱配置正确编译到 Manifest JSON 中。
 */
@ExtendWith(MockitoExtension.class)
class ManifestSnapshotServiceGovernanceTest {

    @Mock private BoundedContextRepository contextRepo;
    @Mock private PublishedManifestRepository manifestRepo;
    @Mock private ModelingService modelingService;
    @Mock private BehaviorService behaviorService;
    @Mock private MetricService metricService;
    @Mock private EventService eventService;
    @Mock private GovernanceService governanceService;

    private ManifestSnapshotService service;

    private static final String CTX_ID = "ctx-test";
    private static final String ROLE_ID = "role-1";

    @BeforeEach
    void setUp() {
        service = new ManifestSnapshotService(contextRepo, manifestRepo, modelingService,
                behaviorService, metricService, eventService, governanceService);
    }

    @Test
    void publishContextShouldIncludeGovernanceSection() throws Exception {
        // given
        BoundedContext ctx = BoundedContext.create("Manufacturing", "manufacturing",
                "Test", null, "tester");
        when(contextRepo.findById(CTX_ID)).thenReturn(Optional.of(ctx));

        when(modelingService.listAggregateRoots(CTX_ID)).thenReturn(List.of());
        when(modelingService.listObjectTypes(CTX_ID)).thenReturn(List.of());
        when(modelingService.listRelationships(CTX_ID)).thenReturn(List.of());
        when(behaviorService.listActions(CTX_ID)).thenReturn(List.of());
        when(behaviorService.listRules(CTX_ID)).thenReturn(List.of());
        when(behaviorService.listDomainEvents(CTX_ID)).thenReturn(List.of());

        Role planner = Role.create(CTX_ID, "Planner", "planner", "Production planner");
        when(governanceService.listRoles(CTX_ID, null)).thenReturn(List.of(planner));

        ObjectPermission op = ObjectPermission.create(ROLE_ID, "ot-1", true, false, false, true);
        when(governanceService.listObjectPermissions(ROLE_ID)).thenReturn(List.of(op));

        FieldPermission fp = FieldPermission.create(ROLE_ID, "ot-1", "cost_price", false, false);
        when(governanceService.listFieldPermissions(ROLE_ID)).thenReturn(List.of(fp));

        ObjectTypeV2 ot = ObjectTypeV2.builder().id("ot-1").code("ProductionOrder").name("PO")
                .contextId(CTX_ID).objectKind("ENTITY").build();
        when(modelingService.getObjectType("ot-1")).thenReturn(ot);

        AgentSandbox sb = AgentSandbox.create("Agent-1", null, ROLE_ID,
                List.of("resolve_intent", "query_ontology", "execute_action"),
                List.of(), List.of(), 10);
        when(governanceService.listSandboxes()).thenReturn(List.of(sb));
        when(governanceService.getRole(ROLE_ID)).thenReturn(planner);

        // when
        PublishedManifest manifest = service.publishContext(CTX_ID);

        // then
        assertThat(manifest).isNotNull();
        assertThat(manifest.getContextId()).isEqualTo(CTX_ID);
        assertThat(manifest.getSnapshotJson()).contains("governance");
    }
}
