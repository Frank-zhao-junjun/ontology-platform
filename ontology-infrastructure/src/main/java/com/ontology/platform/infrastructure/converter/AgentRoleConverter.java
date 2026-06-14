package com.ontology.platform.infrastructure.converter;

import com.ontology.platform.domain.entity.governance.AgentRole;
import com.ontology.platform.infrastructure.persistence.AgentRolePO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AgentRoleConverter {

    public AgentRole toEntity(AgentRolePO po) {
        if (po == null) return null;
        return AgentRole.builder()
                .id(po.getId())
                .tokenId(po.getTokenId())
                .domain(po.getDomain())
                .role(po.getRole())
                .grantedAt(po.getGrantedAt())
                .build();
    }

    public AgentRolePO toPO(AgentRole entity) {
        if (entity == null) return null;
        return AgentRolePO.builder()
                .id(entity.getId())
                .tokenId(entity.getTokenId())
                .domain(entity.getDomain())
                .role(entity.getRole())
                .grantedAt(entity.getGrantedAt())
                .build();
    }

    public List<AgentRole> toEntityList(List<AgentRolePO> poList) {
        if (poList == null) return List.of();
        return poList.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
