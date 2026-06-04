package com.ontology.platform.infrastructure.persistence;

import cn.hutool.json.JSONUtil;
import com.ontology.platform.common.enums.DomainTag;
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
        e.setDomainTag(ctx.getDomainTag() != null ? ctx.getDomainTag().name() : null);
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

    private static String toJson(List<String> list) {
        return JSONUtil.toJsonStr(list != null ? list : Collections.emptyList());
    }

    private static List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) return List.of();
        return JSONUtil.toList(json, String.class);
    }
}
