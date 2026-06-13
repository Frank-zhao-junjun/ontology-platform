package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.AgentToken;
import com.ontology.platform.domain.repository.governance.AgentTokenRepository;
import com.ontology.platform.infrastructure.converter.AgentTokenConverter;
import com.ontology.platform.infrastructure.persistence.AgentTokenPO;
import com.ontology.platform.infrastructure.persistence.AgentTokenPOMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Agent 令牌仓储实现
 * Agent Token Repository Implementation
 * 基于 MyBatis-Plus + PostgreSQL agent_token 表
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class AgentTokenRepositoryImpl implements AgentTokenRepository {

    private final AgentTokenPOMapper agentTokenPOMapper;
    private final AgentTokenConverter agentTokenConverter;

    @Override
    public Optional<AgentToken> findById(String id) {
        log.debug("Finding agent token by id: {}", id);
        AgentTokenPO po = agentTokenPOMapper.selectById(id);
        return Optional.ofNullable(agentTokenConverter.toEntity(po));
    }

    @Override
    public Optional<AgentToken> findByAgentId(String agentId) {
        log.debug("Finding agent token by agentId: {}", agentId);
        AgentTokenPO po = agentTokenPOMapper.selectByAgentId(agentId);
        return Optional.ofNullable(agentTokenConverter.toEntity(po));
    }

    @Override
    public List<AgentToken> findByTenantId(String tenantId) {
        log.debug("Finding agent tokens by tenantId: {}", tenantId);
        List<AgentTokenPO> poList = agentTokenPOMapper.selectByTenantId(tenantId);
        return agentTokenConverter.toEntityList(poList);
    }

    @Override
    public AgentToken save(AgentToken token) {
        log.debug("Saving agent token: id={}, agentId={}", token.getId(), token.getAgentId());
        AgentTokenPO po = agentTokenConverter.toPO(token);
        // upsert 语义：若已存在则更新，否则插入
        if (agentTokenPOMapper.selectById(token.getId()) == null) {
            agentTokenPOMapper.insert(po);
        } else {
            agentTokenPOMapper.updateById(po);
        }
        return token;
    }

    @Override
    public void deleteById(String id) {
        log.debug("Deleting agent token by id: {}", id);
        agentTokenPOMapper.deleteById(id);
    }
}
