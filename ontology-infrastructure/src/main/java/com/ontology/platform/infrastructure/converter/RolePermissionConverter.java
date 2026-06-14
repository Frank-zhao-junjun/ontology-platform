package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.governance.RolePermission;
import com.ontology.platform.infrastructure.persistence.RolePermissionPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RolePermissionConverter {

    public RolePermission toEntity(RolePermissionPO po) {
        if (po == null) return null;
        return RolePermission.builder()
                .id(po.getId())
                .roleId(po.getRoleId())
                .resource(po.getResource())
                .operations(po.getOperationsList())
                .domain(po.getDomain())
                .createdAt(po.getCreatedAt())
                .build();
    }

    public RolePermissionPO toPO(RolePermission entity) {
        if (entity == null) return null;
        RolePermissionPO po = RolePermissionPO.builder()
                .id(entity.getId())
                .roleId(entity.getRoleId())
                .resource(entity.getResource())
                .domain(entity.getDomain())
                .createdAt(entity.getCreatedAt())
                .build();
        po.setOperationsList(entity.getOperations());
        return po;
    }

    public List<RolePermission> toEntityList(List<RolePermissionPO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
