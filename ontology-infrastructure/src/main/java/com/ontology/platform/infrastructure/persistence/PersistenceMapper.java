package com.ontology.platform.infrastructure.persistence;

import cn.hutool.json.JSONUtil;
import com.ontology.platform.common.enums.DomainTag;
import com.ontology.platform.common.enums.EventType;
import com.ontology.platform.common.enums.InvocationMode;
import com.ontology.platform.common.enums.WorkflowState;
import com.ontology.platform.domain.entity.*;
import com.ontology.platform.infrastructure.persistence.entity.*;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

public final class PersistenceMapper {
    private PersistenceMapper() {}

    // ── BoundedContext ──
    public static BoundedContextEntity toEntity(BoundedContext ctx) {
        BoundedContextEntity e = new BoundedContextEntity();
        e.setId(ctx.getId());
        e.setName(ctx.getName());
        e.setCode(ctx.getCode());
        e.setDescription(ctx.getDescription());
        e.setDomainTag(ctx.getDomainTag() != null ? ctx.getDomainTag().getCode() : null);
        e.setOntologyId(ctx.getOntologyId());
        e.setWorkflowState(ctx.getWorkflowState().name());
        e.setCreatedBy(ctx.getCreatedBy());
        e.setCreatedAt(ctx.getCreatedAt());
        e.setUpdatedAt(ctx.getUpdatedAt());
        return e;
    }

    public static BoundedContext toDomain(BoundedContextEntity e) {
        return BoundedContext.rehydrate(e.getId(), e.getName(), e.getCode(), e.getDescription(),
                e.getDomainTag() != null ? DomainTag.fromCode(e.getDomainTag()) : null,
                e.getOntologyId(), WorkflowState.valueOf(e.getWorkflowState()),
                e.getCreatedBy(), e.getCreatedAt(), e.getUpdatedAt());
    }

    // ── AggregateRoot ──
    public static AggregateRootEntity toEntity(AggregateRoot ar) {
        AggregateRootEntity e = new AggregateRootEntity();
        e.setId(ar.getId());
        e.setContextId(ar.getContextId());
        e.setName(ar.getName());
        e.setCode(ar.getCode());
        e.setDescription(ar.getDescription());
        e.setActive(ar.isActive());
        e.setCreatedAt(ar.getCreatedAt());
        return e;
    }

    public static AggregateRoot toDomain(AggregateRootEntity e) {
        return AggregateRoot.builder().id(e.getId()).contextId(e.getContextId()).name(e.getName())
                .code(e.getCode()).description(e.getDescription()).active(e.isActive()).build();
    }

    // ── ObjectType ──
    public static ObjectTypeEntity toEntity(ObjectTypeV2 ot) {
        ObjectTypeEntity e = new ObjectTypeEntity();
        e.setId(ot.getId());
        e.setContextId(ot.getContextId());
        e.setAggregateRootId(ot.getAggregateRootId());
        e.setParentObjectId(ot.getParentObjectId());
        e.setName(ot.getName());
        e.setCode(ot.getCode());
        e.setObjectKind(ot.getObjectKind());
        e.setDescription(ot.getDescription());
        e.setAttributes(ot.getAttributes());
        e.setCreatedAt(ot.getCreatedAt());
        return e;
    }

    public static ObjectTypeV2 toDomain(ObjectTypeEntity e) {
        return ObjectTypeV2.builder().id(e.getId()).contextId(e.getContextId())
                .aggregateRootId(e.getAggregateRootId()).parentObjectId(e.getParentObjectId())
                .name(e.getName()).code(e.getCode()).objectKind(e.getObjectKind())
                .description(e.getDescription()).attributes(e.getAttributes()).build();
    }

    // ── Relationship ──
    public static RelationshipEntity toEntity(Relationship r) {
        RelationshipEntity e = new RelationshipEntity();
        e.setId(r.getId());
        e.setContextId(r.getContextId());
        e.setName(r.getName());
        e.setCode(r.getCode());
        e.setSourceObjectId(r.getSourceObjectId());
        e.setTargetObjectId(r.getTargetObjectId());
        e.setCardinality(r.getCardinality());
        e.setRelationKind(r.getRelationKind());
        e.setCrossContext(r.isCrossContext());
        e.setTargetContextId(r.getTargetContextId());
        e.setCreatedAt(r.getCreatedAt());
        return e;
    }

    public static Relationship toDomain(RelationshipEntity e) {
        return Relationship.builder().id(e.getId()).contextId(e.getContextId())
                .sourceObjectId(e.getSourceObjectId()).targetObjectId(e.getTargetObjectId())
                .name(e.getName()).code(e.getCode()).cardinality(e.getCardinality())
                .relationKind(e.getRelationKind()).crossContext(e.isCrossContext())
                .targetContextId(e.getTargetContextId()).build();
    }

    // ── DataSource ──
    public static DataSourceEntity toEntity(DataSource ds) {
        DataSourceEntity e = new DataSourceEntity();
        e.setId(ds.getId());
        e.setName(ds.getName());
        e.setCode(ds.getCode());
        e.setSourceType(ds.getSourceType());
        e.setConnectionConfig(ds.getConnectionConfig());
        e.setCredentialRef(ds.getCredentialRef());
        e.setActive(ds.isActive());
        e.setCreatedAt(ds.getCreatedAt());
        return e;
    }

    public static DataSource toDomain(DataSourceEntity e) {
        return DataSource.rehydrate(e.getId(), e.getName(), e.getCode(), e.getSourceType(),
                e.getConnectionConfig(), e.getCredentialRef(), e.isActive(), e.getCreatedAt());
    }

    // ── DataAccessMethod ──
    public static DataAccessMethodEntity toEntity(DataAccessMethod m) {
        DataAccessMethodEntity e = new DataAccessMethodEntity();
        e.setId(m.getId());
        e.setContextId(m.getContextId());
        e.setObjectTypeId(m.getObjectTypeId());
        e.setDataSourceId(m.getDataSourceId());
        e.setMethodType(m.getMethodType());
        e.setAccessConfig(m.getAccessConfig());
        e.setCacheTtlSec(m.getCacheTtlSec());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }

    // ── Role ──
    public static RoleEntity toEntity(Role r) {
        RoleEntity e = new RoleEntity();
        e.setId(r.getId());
        e.setContextId(r.getContextId());
        e.setName(r.getName());
        e.setCode(r.getCode());
        e.setDescription(r.getDescription());
        e.setGlobal(r.isGlobal());
        e.setCreatedAt(r.getCreatedAt());
        return e;
    }

    public static Role toDomain(RoleEntity e) {
        return Role.rehydrate(e.getId(), e.getContextId(), e.getName(), e.getCode(), e.getDescription(),
                e.isGlobal(), e.getCreatedAt());
    }

    // ── ObjectPermission ──
    public static RolePermissionEntity toEntity(ObjectPermission p) {
        RolePermissionEntity e = new RolePermissionEntity();
        e.setId(p.getId());
        e.setRoleId(p.getRoleId());
        e.setObjectTypeId(p.getObjectTypeId());
        e.setPermRead(p.isPermRead());
        e.setPermWrite(p.isPermWrite());
        e.setPermDelete(p.isPermDelete());
        e.setPermExecute(p.isPermExecute());
        e.setCreatedAt(p.getCreatedAt());
        return e;
    }

    public static ObjectPermission toDomain(RolePermissionEntity e) {
        return ObjectPermission.rehydrate(e.getId(), e.getRoleId(), e.getObjectTypeId(),
                e.isPermRead(), e.isPermWrite(), e.isPermDelete(), e.isPermExecute(), e.getCreatedAt());
    }

    // ── FieldPermission ──
    public static FieldPermissionEntity toEntity(FieldPermission p) {
        FieldPermissionEntity e = new FieldPermissionEntity();
        e.setId(p.getId());
        e.setRoleId(p.getRoleId());
        e.setObjectTypeId(p.getObjectTypeId());
        e.setFieldName(p.getFieldName());
        e.setVisible(p.isVisible());
        e.setEditable(p.isEditable());
        e.setCreatedAt(p.getCreatedAt());
        return e;
    }

    public static FieldPermission toDomain(FieldPermissionEntity e) {
        return FieldPermission.rehydrate(e.getId(), e.getRoleId(), e.getObjectTypeId(), e.getFieldName(),
                e.isVisible(), e.isEditable(), e.getCreatedAt());
    }

    // ── AgentSandbox ──
    public static AgentSandboxEntity toEntity(AgentSandbox sb) {
        AgentSandboxEntity e = new AgentSandboxEntity();
        e.setId(sb.getId());
        e.setName(sb.getName());
        e.setManifestVersionId(sb.getManifestVersionId());
        e.setAgentRoleId(sb.getAgentRoleId());
        e.setAllowedTools(toJson(sb.getAllowedTools()));
        e.setAllowedAggregateRoots(toJson(sb.getAllowedAggregateRoots()));
        e.setAllowedBehaviors(toJson(sb.getAllowedBehaviors()));
        e.setMaxOpsPerSecond(sb.getMaxOpsPerSecond());
        e.setActive(sb.isActive());
        e.setCreatedAt(sb.getCreatedAt());
        return e;
    }

    public static AgentSandbox toDomain(AgentSandboxEntity e) {
        return AgentSandbox.rehydrate(e.getId(), e.getName(), e.getManifestVersionId(), e.getAgentRoleId(),
                fromJsonList(e.getAllowedTools()), fromJsonList(e.getAllowedAggregateRoots()),
                fromJsonList(e.getAllowedBehaviors()), e.getMaxOpsPerSecond(), e.isActive(), e.getCreatedAt());
    }

    // ── ValidationRule ──
    public static ValidationRuleEntity toEntity(ValidationRule r) {
        ValidationRuleEntity e = new ValidationRuleEntity();
        e.setId(r.getId());
        e.setContextId(r.getContextId());
        e.setManifestCode(r.getManifestCode());
        e.setName(r.getName());
        e.setRuleType(r.getRuleType());
        e.setExpressionJson(r.getExpressionJson());
        e.setErrorMessage(r.getErrorMessage());
        e.setFailurePayloadSchema(r.getFailurePayloadSchema());
        e.setEnabled(r.isEnabled());
        e.setCreatedAt(r.getCreatedAt());
        return e;
    }

    public static ValidationRule toDomain(ValidationRuleEntity e) {
        return ValidationRule.builder().id(e.getId()).contextId(e.getContextId())
                .manifestCode(e.getManifestCode()).name(e.getName()).ruleType(e.getRuleType())
                .expressionJson(e.getExpressionJson()).errorMessage(e.getErrorMessage())
                .failurePayloadSchema(e.getFailurePayloadSchema()).enabled(e.isEnabled())
                .createdAt(e.getCreatedAt()).build();
    }

    // ── OntologyAction ──
    public static OntologyActionEntity toEntity(OntologyAction a) {
        OntologyActionEntity e = new OntologyActionEntity();
        e.setId(a.getId());
        e.setContextId(a.getContextId());
        e.setManifestCode(a.getManifestCode());
        e.setName(a.getName());
        e.setNameEn(a.getNameEn());
        e.setDescription(a.getDescription());
        e.setAggregateRootId(a.getAggregateRootId());
        e.setInvocationMode(a.getInvocationMode().name());
        e.setParametersJson(a.getParametersJson());
        e.setPublishesEventIdsJson(a.getPublishesEventIdsJson());
        e.setAllowedStateFromJson(a.getAllowedStateFromJson());
        e.setBusinessScenarioIdsJson(a.getBusinessScenarioIdsJson());
        e.setMcpToolName(a.getMcpToolName());
        e.setCreatedAt(a.getCreatedAt());
        return e;
    }

    public static OntologyAction toDomain(OntologyActionEntity e) {
        return OntologyAction.builder().id(e.getId()).contextId(e.getContextId())
                .manifestCode(e.getManifestCode()).name(e.getName()).nameEn(e.getNameEn())
                .description(e.getDescription()).aggregateRootId(e.getAggregateRootId())
                .invocationMode(InvocationMode.fromCode(e.getInvocationMode()))
                .parametersJson(e.getParametersJson()).publishesEventIdsJson(e.getPublishesEventIdsJson())
                .allowedStateFromJson(e.getAllowedStateFromJson())
                .businessScenarioIdsJson(e.getBusinessScenarioIdsJson())
                .mcpToolName(e.getMcpToolName()).createdAt(e.getCreatedAt()).build();
    }

    // ── DomainEvent ──
    public static DomainEventEntity toEntity(DomainEventDefinition ev) {
        DomainEventEntity e = new DomainEventEntity();
        e.setId(ev.getId());
        e.setContextId(ev.getContextId());
        e.setManifestCode(ev.getManifestCode());
        e.setName(ev.getName());
        e.setNameEn(ev.getNameEn());
        e.setEventType(ev.getEventType().getCode());
        e.setAggregateRootId(ev.getAggregateRootId());
        e.setTriggerActionId(ev.getTriggerActionId());
        e.setPayloadSchemaJson(ev.getPayloadSchemaJson());
        e.setCreatedAt(ev.getCreatedAt());
        return e;
    }

    public static DomainEventDefinition toDomain(DomainEventEntity e) {
        return DomainEventDefinition.builder().id(e.getId()).contextId(e.getContextId())
                .manifestCode(e.getManifestCode()).name(e.getName()).nameEn(e.getNameEn())
                .eventType(EventType.fromCode(e.getEventType()))
                .aggregateRootId(e.getAggregateRootId()).triggerActionId(e.getTriggerActionId())
                .payloadSchemaJson(e.getPayloadSchemaJson()).createdAt(e.getCreatedAt()).build();
    }

    // ── EventRoute ──
    public static EventRouteEntity toEntity(EventRoute r) {
        EventRouteEntity e = new EventRouteEntity();
        e.setId(r.getId());
        e.setContextId(r.getContextId());
        e.setManifestCode(r.getManifestCode());
        e.setSourceEventId(r.getSourceEventId());
        e.setRouteTargetsJson(r.getRouteTargetsJson());
        e.setFilterConditionsJson(r.getFilterConditionsJson());
        e.setCreatedAt(r.getCreatedAt());
        return e;
    }

    public static EventRoute toDomain(EventRouteEntity e) {
        return EventRoute.builder().id(e.getId()).contextId(e.getContextId())
                .manifestCode(e.getManifestCode()).sourceEventId(e.getSourceEventId())
                .routeTargetsJson(e.getRouteTargetsJson()).filterConditionsJson(e.getFilterConditionsJson())
                .createdAt(e.getCreatedAt()).build();
    }

    // ── EventHandler ──
    public static EventHandlerEntity toEntity(EventHandler h) {
        EventHandlerEntity e = new EventHandlerEntity();
        e.setId(h.getId());
        e.setContextId(h.getContextId());
        e.setManifestCode(h.getManifestCode());
        e.setEventId(h.getEventId());
        e.setHandlerBehaviorId(h.getHandlerBehaviorId());
        e.setScenarioId(h.getScenarioId());
        e.setPreconditionState(h.getPreconditionState());
        e.setPriority(h.getPriority());
        e.setExecutionMode(h.getExecutionMode());
        e.setCreatedAt(h.getCreatedAt());
        return e;
    }

    public static EventHandler toDomain(EventHandlerEntity e) {
        return EventHandler.builder().id(e.getId()).contextId(e.getContextId())
                .manifestCode(e.getManifestCode()).eventId(e.getEventId())
                .handlerBehaviorId(e.getHandlerBehaviorId()).scenarioId(e.getScenarioId())
                .preconditionState(e.getPreconditionState()).priority(e.getPriority())
                .executionMode(e.getExecutionMode()).createdAt(e.getCreatedAt()).build();
    }

    // ── PublishedManifest ──
    public static PublishedManifestEntity toEntity(PublishedManifest m) {
        PublishedManifestEntity e = new PublishedManifestEntity();
        e.setId(m.getId());
        e.setContextId(m.getContextId());
        e.setOntologyId(m.getOntologyId());
        e.setVersion(m.getVersion());
        e.setApiVersion(m.getApiVersion());
        e.setStatus(m.getStatus());
        e.setSnapshotJson(m.getSnapshotJson());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }

    public static PublishedManifest toDomain(PublishedManifestEntity e) {
        return PublishedManifest.builder().id(e.getId()).contextId(e.getContextId())
                .ontologyId(e.getOntologyId()).version(e.getVersion()).apiVersion(e.getApiVersion())
                .status(e.getStatus()).snapshotJson(e.getSnapshotJson()).createdAt(e.getCreatedAt()).build();
    }

    private static String toJson(List<String> list) {
        return JSONUtil.toJsonStr(list != null ? list : Collections.emptyList());
    }

    private static List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        return JSONUtil.toList(json, String.class);
    }

    // ── Metric ──
    public static MetricEntity toEntity(Metric m) {
        MetricEntity e = new MetricEntity();
        e.setId(m.getId());
        e.setContextId(m.getContextId());
        e.setManifestCode(m.getManifestCode());
        e.setName(m.getName());
        e.setNameEn(m.getNameEn());
        e.setFormula(m.getFormula());
        e.setDataSourceRefJson(m.getDataSourceRefJson());
        e.setAggregationDimensionsJson(m.getAggregationDimensionsJson());
        e.setPeriod(m.getPeriod());
        e.setCreatedAt(m.getCreatedAt());
        return e;
    }

    public static Metric toDomain(MetricEntity e) {
        return Metric.builder().id(e.getId()).contextId(e.getContextId())
                .manifestCode(e.getManifestCode()).name(e.getName()).nameEn(e.getNameEn())
                .formula(e.getFormula()).dataSourceRefJson(e.getDataSourceRefJson())
                .aggregationDimensionsJson(e.getAggregationDimensionsJson())
                .period(e.getPeriod()).createdAt(e.getCreatedAt()).build();
    }

    // ── WorkflowStateLog ──
    public static WorkflowStateLogEntity toEntity(WorkflowStateLog log) {
        WorkflowStateLogEntity e = new WorkflowStateLogEntity();
        e.setId(log.getId());
        e.setContextId(log.getContextId());
        e.setFromState(log.getFromState());
        e.setToState(log.getToState());
        e.setOperatedBy(log.getOperatedBy());
        e.setOperatedAt(log.getOperatedAt());
        e.setComment(log.getComment());
        return e;
    }

    public static WorkflowStateLog toDomain(WorkflowStateLogEntity e) {
        return WorkflowStateLog.builder().id(e.getId()).contextId(e.getContextId())
                .fromState(e.getFromState()).toState(e.getToState())
                .operatedBy(e.getOperatedBy()).comment(e.getComment())
                .operatedAt(e.getOperatedAt()).build();
    }

    // ── ReviewComment ──
    public static ReviewCommentEntity toEntity(ReviewComment c) {
        ReviewCommentEntity e = new ReviewCommentEntity();
        e.setId(c.getId());
        e.setContextId(c.getContextId());
        e.setTargetType(c.getTargetType());
        e.setTargetId(c.getTargetId());
        e.setReviewer(c.getReviewer());
        e.setResolution(c.getResolution());
        e.setContent(c.getContent());
        e.setCreatedAt(c.getCreatedAt());
        e.setResolvedAt(c.getResolvedAt());
        return e;
    }

    public static ReviewComment toDomain(ReviewCommentEntity e) {
        return ReviewComment.builder().id(e.getId()).contextId(e.getContextId())
                .targetType(e.getTargetType()).targetId(e.getTargetId())
                .reviewer(e.getReviewer()).resolution(e.getResolution())
                .content(e.getContent()).createdAt(e.getCreatedAt())
                .resolvedAt(e.getResolvedAt()).build();
    }

    // ── StateMachine ──
    public static StateMachineEntity toEntity(StateMachine sm) {
        StateMachineEntity e = new StateMachineEntity();
        e.setId(sm.getId());
        e.setContextId(sm.getContextId());
        e.setName(sm.getName());
        e.setNameEn(sm.getNameEn());
        e.setObjectTypeId(sm.getObjectTypeId());
        e.setStatusField(sm.getStatusField());
        e.setStatesJson(sm.getStatesJson());
        e.setTransitionsJson(sm.getTransitionsJson());
        e.setCreatedAt(sm.getCreatedAt());
        e.setUpdatedAt(sm.getUpdatedAt());
        return e;
    }

    public static StateMachine toDomain(StateMachineEntity e) {
        return StateMachine.builder().id(e.getId()).contextId(e.getContextId())
                .name(e.getName()).nameEn(e.getNameEn()).objectTypeId(e.getObjectTypeId())
                .statusField(e.getStatusField()).statesJson(e.getStatesJson())
                .transitionsJson(e.getTransitionsJson())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build();
    }

    // ── ValueObject ──
    public static ValueObjectEntity toEntity(ValueObject vo) {
        ValueObjectEntity e = new ValueObjectEntity();
        e.setId(vo.getId());
        e.setName(vo.getName());
        e.setCode(vo.getCode());
        e.setNameEn(vo.getNameEn());
        e.setDescription(vo.getDescription());
        e.setPropertiesJson(vo.getPropertiesJson());
        e.setCreatedAt(vo.getCreatedAt());
        e.setUpdatedAt(vo.getUpdatedAt());
        return e;
    }

    public static ValueObject toDomain(ValueObjectEntity e) {
        return ValueObject.builder().id(e.getId()).name(e.getName()).code(e.getCode())
                .nameEn(e.getNameEn()).description(e.getDescription())
                .propertiesJson(e.getPropertiesJson()).createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt()).build();
    }

    // ── BusinessScenario ──
    public static BusinessScenarioEntity toEntity(BusinessScenario s) {
        BusinessScenarioEntity e = new BusinessScenarioEntity();
        e.setId(s.getId());
        e.setContextId(s.getContextId());
        e.setName(s.getName());
        e.setCode(s.getCode());
        e.setNameEn(s.getNameEn());
        e.setDescription(s.getDescription());
        e.setApplicableObjectTypeIdsJson(s.getApplicableObjectTypeIdsJson());
        e.setCreatedAt(s.getCreatedAt());
        return e;
    }

    public static BusinessScenario toDomain(BusinessScenarioEntity e) {
        return BusinessScenario.builder().id(e.getId()).contextId(e.getContextId())
                .name(e.getName()).code(e.getCode()).nameEn(e.getNameEn())
                .description(e.getDescription())
                .applicableObjectTypeIdsJson(e.getApplicableObjectTypeIdsJson())
                .createdAt(e.getCreatedAt()).build();
    }

    // ── AuditLog ──
    public static AuditLogEntity toEntity(AuditLog log) {
        AuditLogEntity e = new AuditLogEntity();
        e.setId(log.getId());
        e.setTenantId(log.getTenantId());
        e.setApiKeyName(log.getApiKeyName());
        e.setSandboxId(log.getSandboxId());
        e.setAgentRoleName(log.getAgentRoleName());
        e.setAction(log.getAction());
        e.setActionType(log.getActionType());
        e.setObjectType(log.getObjectType());
        e.setObjectId(log.getObjectId());
        e.setRequestPath(log.getRequestPath());
        e.setResponseCode(log.getResponseCode());
        e.setErrorMessage(log.getErrorMessage());
        e.setExecutionTimeMs(log.getExecutionTimeMs());
        e.setTimestamp(log.getTimestamp());
        return e;
    }

    public static AuditLog fromEntity(AuditLogEntity e) {
        return AuditLog.rehydrate(e.getId(), e.getTenantId(), e.getApiKeyName(),
                e.getSandboxId(), e.getAgentRoleName(), e.getAction(), e.getActionType(),
                e.getObjectType(), e.getObjectId(), e.getRequestPath(),
                e.getResponseCode(), e.getErrorMessage(), e.getExecutionTimeMs(),
                e.getTimestamp());
    }
}
