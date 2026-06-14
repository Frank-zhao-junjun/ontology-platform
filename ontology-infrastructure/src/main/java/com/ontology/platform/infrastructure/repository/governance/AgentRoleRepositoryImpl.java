package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.AgentRole;
import com.ontology.platform.domain.repository.governance.AgentRoleRepository;
import com.ontology.platform.infrastructure.converter.AgentRoleConverter;
import com.ontology.platform.infrastructure.persistence.AgentRolePO;
import com.ontology.platform.infrastructure.persistence.AgentRolePOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AgentRoleRepositoryImpl implements AgentRoleRepository {

    private final AgentRolePOMapper agentRolePOMapper;
    private final AgentRoleConverter agentRoleConverter;

    @Override
    public Optional<AgentRole> findById(String id) {
        log.debug("Finding agent role by id: {}", id);
        AgentRolePO po = agentRolePOMapper.selectById(id);
        return Optional.ofNullable(agentRoleConverter.toEntity(po));
    }

    @Override
    public List<AgentRole> findByTokenId(String tokenId) {
        log.debug("Finding agent roles by tokenId: {}", tokenId);
        List<AgentRolePO> poList = agentRolePOMapper.selectByTokenId(tokenId);
        return agentRoleConverter.toEntityList(poList);
    }

    @Override
    public List<AgentRole> findByDomain(String domain) {
        log.debug("Finding agent roles by domain: {}", domain);
        List<AgentRolePO> poList = agentRolePOMapper.selectByDomain(domain);
        return agentRoleConverter.toEntityList(poList);
    }

    @Override
    public AgentRole save(AgentRole role) {
        log.debug("Saving agent role: {}", role.getId());
        AgentRolePO po = agentRoleConverter.toPO(role);
        agentRolePOMapper.insert(po);
        return role;
    }
}
