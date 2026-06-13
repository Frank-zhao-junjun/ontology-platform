package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Agent 令牌持久化 Mapper 接口
 * Agent Token Persistence Mapper Interface
 * 对应表：agent_token
 */
@Mapper
public interface AgentTokenPOMapper extends BaseMapper<AgentTokenPO> {

    /**
     * 根据代理ID查询令牌
     */
    AgentTokenPO selectByAgentId(@Param("agentId") String agentId);

    /**
     * 根据租户ID查询所有令牌
     */
    List<AgentTokenPO> selectByTenantId(@Param("tenantId") String tenantId);

    /**
     * 统计指定代理ID的令牌数量
     */
    long countByAgentId(@Param("agentId") String agentId);
}
