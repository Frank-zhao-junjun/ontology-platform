package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AgentRolePOMapper extends BaseMapper<AgentRolePO> {
    List<AgentRolePO> selectByTokenId(@Param("tokenId") String tokenId);
    List<AgentRolePO> selectByDomain(@Param("domain") String domain);
}
