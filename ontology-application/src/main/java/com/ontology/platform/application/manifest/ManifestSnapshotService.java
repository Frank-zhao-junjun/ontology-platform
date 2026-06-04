package com.ontology.platform.application.manifest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ontology.platform.application.service.BehaviorService;
import com.ontology.platform.application.service.ModelingService;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import com.ontology.platform.domain.entity.BoundedContext;
import com.ontology.platform.domain.entity.DomainEventDefinition;
import com.ontology.platform.domain.entity.OntologyAction;
import com.ontology.platform.domain.entity.PublishedManifest;
import com.ontology.platform.domain.entity.ValidationRule;
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

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    private String resolveArManifestCode(String contextId, String aggregateRootId) {
        return modelingService.listAggregateRoots(contextId).stream()
                .filter(ar -> ar.getId().equals(aggregateRootId))
                .map(ar -> ar.getCode())
                .findFirst()
                .orElse(aggregateRootId);
    }
}
