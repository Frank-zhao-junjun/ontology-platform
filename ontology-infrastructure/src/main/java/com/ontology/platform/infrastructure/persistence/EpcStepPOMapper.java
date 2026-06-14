package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface EpcStepPOMapper extends BaseMapper<EpcStepPO> {
    List<EpcStepPO> selectByOntologyId(@Param("ontologyId") String ontologyId);
    List<EpcStepPO> selectByOntologyIdAndFlowName(@Param("ontologyId") String ontologyId, @Param("flowName") String flowName);
    List<EpcStepPO> selectByFlowNameOrderByStepOrder(@Param("flowName") String flowName);
}
