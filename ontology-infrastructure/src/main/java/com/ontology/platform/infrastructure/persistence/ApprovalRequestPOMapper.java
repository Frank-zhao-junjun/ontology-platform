package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ApprovalRequestPOMapper extends BaseMapper<ApprovalRequestPO> {
    List<ApprovalRequestPO> selectByAgentId(@Param("agentId") String agentId);
    List<ApprovalRequestPO> selectPending();
}
