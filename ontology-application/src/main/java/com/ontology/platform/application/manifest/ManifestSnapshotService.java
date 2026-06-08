package com.ontology.platform.application.manifest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ontology.platform.application.service.BehaviorService;
import com.ontology.platform.application.service.EventService;
import com.ontology.platform.application.service.GovernanceService;
import com.ontology.platform.application.service.MetricService;
import com.ontology.platform.application.service.ModelingService;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.*;
import com.ontology.platform.domain.repository.BoundedContextRepository;
import com.ontology.platform.domain.repository.PublishedManifestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * US-A01 Round 3: compile published snapshot from DB on approve.
 */
@Service
@RequiredArgsConstructor
public class ManifestSnapshotService {
    private final BoundedContextRepository contextRepo;
    private final PublishedManifestRepository manifestRepo;
    private final ModelingService modelingService;
    private final BehaviorService behaviorService;
    private final MetricService metricService;
    private final EventService eventService;
    private final GovernanceService governanceService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public PublishedManifest publishContext(String contextId) throws Exception {
        BoundedContext ctx = contextRepo.findById(contextId)
                .orElseThrow(() -> new ResourceNotFoundException("Context not found: " + contextId));
        String version = nextVersion(contextId);
        String snapshotJson = buildSnapshotJson(ctx, version);
        PublishedManifest published = PublishedManifest.create(contextId, ctx.getOntologyId(), version, snapshotJson);
        manifestRepo.save(published);
        return published;
    }

    public List<PublishedManifest> listManifests(String contextId) {
        return manifestRepo.findByContextId(contextId);
    }

    public PublishedManifest getLatest(String contextId) {
        return manifestRepo.findLatestByContextId(contextId)
                .orElseThrow(() -> new ResourceNotFoundException("No published manifest for context: " + contextId));
    }

    private String nextVersion(String contextId) {
        int n = manifestRepo.countByContextId(contextId);
        return "0.1." + (n + 1);
    }

    private String buildSnapshotJson(BoundedContext ctx, String version) throws Exception {
        String contextId = ctx.getId();
        ObjectNode root = objectMapper.createObjectNode();
        root.put("apiVersion", "ontology.platform/v1");
        root.put("kind", "OntologyManifest");

        ObjectNode metadata = root.putObject("metadata");
        metadata.put("id", ctx.getOntologyId());
        metadata.put("version", version);
        metadata.put("name", ctx.getName());
        metadata.put("boundedContext", ctx.getName());
        metadata.put("status", "published");
        metadata.put("compiledAt", Instant.now().toString());
        metadata.put("source", "ontology-platform");

        ObjectNode spec = root.putObject("spec");
        ObjectNode semantic = spec.putObject("semantic");
        ObjectNode bc = semantic.putObject("boundedContext");
        bc.put("id", ctx.getCode());
        bc.put("name", ctx.getName());

        ArrayNode objectTypes = semantic.putArray("objectTypes");
        modelingService.listObjectTypes(contextId).forEach(ot -> {
            ObjectNode node = objectTypes.addObject();
            node.put("id", ot.getCode());
            node.put("name", ot.getName());
            node.put("code", ot.getCode());
            node.put("objectKind", ot.getObjectKind());
        });

        ObjectNode behavior = spec.putObject("behavior");
        ArrayNode actions = behavior.putArray("actions");
        for (OntologyAction a : behaviorService.listActions(contextId)) {
            ObjectNode node = actions.addObject();
            node.put("id", a.getManifestCode());
            node.put("name", a.getName());
            node.put("nameEn", a.getNameEn());
            node.put("aggregateRootId", resolveArManifestCode(contextId, a.getAggregateRootId()));
            ArrayNode preRuleIds = objectMapper.createArrayNode();
            for (String ruleId : behaviorService.listRuleIdsForAction(a.getId())) {
                preRuleIds.add(behaviorService.getRule(ruleId).getManifestCode());
            }
            node.set("preRuleIds", preRuleIds);
            node.set("parameters", objectMapper.readTree(a.getParametersJson()));
        }

        ArrayNode rules = behavior.putArray("rules");
        for (ValidationRule r : behaviorService.listRules(contextId)) {
            ObjectNode node = rules.addObject();
            node.put("id", r.getManifestCode());
            node.put("name", r.getName());
            node.put("type", r.getRuleType());
            node.set("expression", objectMapper.readTree(r.getExpressionJson()));
            node.put("errorMessage", r.getErrorMessage());
        }

        ArrayNode metrics = behavior.putArray("metrics");
        for (Metric m : metricService.listMetrics(contextId)) {
            ObjectNode node = metrics.addObject();
            node.put("id", m.getManifestCode());
            node.put("name", m.getName());
            node.put("nameEn", m.getNameEn());
            node.put("formula", m.getFormula());
            node.set("dataSourceRefs", objectMapper.readTree(m.getDataSourceRefJson()));
            node.set("aggregationDimensions", objectMapper.readTree(m.getAggregationDimensionsJson()));
            if (m.getPeriod() != null && !m.getPeriod().isBlank()) {
                node.put("period", m.getPeriod());
            }
        }

        ObjectNode events = spec.putObject("events");
        ArrayNode domainEvents = events.putArray("domainEvents");
        for (DomainEventDefinition ev : behaviorService.listDomainEvents(contextId)) {
            ObjectNode node = domainEvents.addObject();
            node.put("id", ev.getManifestCode());
            node.put("name", ev.getName());
            node.put("nameEn", ev.getNameEn());
            node.put("aggregateRootId", resolveArManifestCode(contextId, ev.getAggregateRootId()));
            if (ev.getTriggerActionId() != null) {
                node.put("triggerActionId", behaviorService.getAction(ev.getTriggerActionId()).getManifestCode());
            }
            node.set("payloadSchema", objectMapper.readTree(ev.getPayloadSchemaJson()));
        }

        // event routes (US-E03)
        ArrayNode eventRoutes = events.putArray("eventRoutes");
        for (EventRoute r : eventService.listEventRoutes(contextId)) {
            ObjectNode node = eventRoutes.addObject();
            node.put("id", r.getManifestCode());
            node.put("sourceEventId", resolveEventManifestCode(r.getSourceEventId()));
            node.set("routeTargets", objectMapper.readTree(r.getRouteTargetsJson()));
            if (r.getFilterConditionsJson() != null && !r.getFilterConditionsJson().equals("[]")) {
                node.set("filterConditions", objectMapper.readTree(r.getFilterConditionsJson()));
            }
        }

        // event handlers matrix (US-E04)
        ArrayNode eventHandlers = events.putArray("eventHandlers");
        for (EventHandler h : eventService.listEventHandlers(contextId)) {
            ObjectNode node = eventHandlers.addObject();
            node.put("id", h.getManifestCode());
            node.put("eventId", resolveEventManifestCode(h.getEventId()));
            node.put("handlerBehaviorId", behaviorService.getAction(h.getHandlerBehaviorId()).getManifestCode());
            if (h.getScenarioId() != null) node.put("scenarioId", h.getScenarioId());
            if (h.getPreconditionState() != null) node.put("preconditionState", h.getPreconditionState());
            node.put("priority", h.getPriority());
            node.put("executionMode", h.getExecutionMode());
        }

        // ── governance (US-G01/G02/G04) ──
        ObjectNode governance = spec.putObject("governance");

        // roles + object permissions + field permissions
        ArrayNode roles = governance.putArray("roles");
        for (Role role : governanceService.listRoles(contextId, null)) {
            ObjectNode roleNode = roles.addObject();
            roleNode.put("id", role.getCode());
            roleNode.put("name", role.getName());
            if (role.getDescription() != null) roleNode.put("description", role.getDescription());
            roleNode.put("isGlobal", role.isGlobal());

            // object-level permissions (G01 AC-3)
            ArrayNode perms = roleNode.putArray("permissions");
            for (ObjectPermission op : governanceService.listObjectPermissions(role.getId())) {
                ObjectNode permNode = perms.addObject();
                permNode.put("objectTypeId", resolveObjectTypeCode(op.getObjectTypeId()));
                ArrayNode ops = permNode.putArray("ops");
                if (op.isPermRead()) ops.add("READ");
                if (op.isPermWrite()) ops.add("WRITE");
                if (op.isPermDelete()) ops.add("DELETE");
                if (op.isPermExecute()) ops.add("EXECUTE");
            }
        }

        // field-level permissions (G02 AC-2)
        ArrayNode fieldPerms = governance.putArray("fieldPermissions");
        for (Role role : governanceService.listRoles(contextId, null)) {
            for (FieldPermission fp : governanceService.listFieldPermissions(role.getId())) {
                ObjectNode fpNode = fieldPerms.addObject();
                fpNode.put("objectTypeId", resolveObjectTypeCode(fp.getObjectTypeId()));
                fpNode.put("propertyNameEn", fp.getFieldName());
                fpNode.put("isVisible", fp.isVisible());
                fpNode.put("isEditable", fp.isEditable());
                fpNode.put("roleId", role.getCode());
            }
        }

        // agent sandboxes (G04 AC-1)
        ArrayNode agentPolicies = governance.putArray("agentPolicies");
        for (AgentSandbox sb : governanceService.listSandboxes()) {
            // only include sandboxes bound to this context's roles
            if (sb.getAgentRoleId() != null) {
                try {
                    Role ar = governanceService.getRole(sb.getAgentRoleId());
                    if (!contextId.equals(ar.getContextId()) && !ar.isGlobal()) continue;
                } catch (Exception ignore) { continue; }
            }
            ObjectNode sbNode = agentPolicies.addObject();
            sbNode.put("id", sb.getName());
            if (sb.getManifestVersionId() != null) sbNode.put("manifestVersion", sb.getManifestVersionId());
            if (sb.getAgentRoleId() != null) {
                try {
                    Role ar = governanceService.getRole(sb.getAgentRoleId());
                    sbNode.put("roleId", ar.getCode());
                } catch (Exception ignore) {}
            }
            sbNode.set("allowedMcpTools", objectMapper.valueToTree(sb.getAllowedTools()));
            sbNode.set("allowedAggregateRootIds", objectMapper.valueToTree(sb.getAllowedAggregateRoots()));
            sbNode.set("allowedActionIds", objectMapper.valueToTree(sb.getAllowedBehaviors()));
            ObjectNode rateLimit = sbNode.putObject("rateLimit");
            rateLimit.put("maxCallsPerSecond", sb.getMaxOpsPerSecond());
            sbNode.put("defaultDeny", true);
        }

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    private String resolveArManifestCode(String contextId, String aggregateRootId) {
        return modelingService.listAggregateRoots(contextId).stream()
                .filter(ar -> ar.getId().equals(aggregateRootId))
                .map(ar -> ar.getCode())
                .findFirst()
                .orElse(aggregateRootId);
    }

    private String resolveObjectTypeCode(String objectTypeId) {
        try {
            return modelingService.getObjectType(objectTypeId).getCode();
        } catch (Exception e) {
            return objectTypeId;
        }
    }

    private String resolveEventManifestCode(String eventId) {
        try {
            return behaviorService.getDomainEvent(eventId).getManifestCode();
        } catch (Exception e) {
            return eventId;
        }
    }
}
