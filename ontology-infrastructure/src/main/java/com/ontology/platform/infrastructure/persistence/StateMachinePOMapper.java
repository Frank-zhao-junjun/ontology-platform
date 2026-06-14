package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface StateMachinePOMapper extends BaseMapper<StateMachinePO> {
    List<StateMachinePO> selectByOntologyId(@Param("ontologyId") String ontologyId);
    List<StateMachinePO> selectByOntologyIdAndEntityId(@Param("ontologyId") String ontologyId, @Param("entityId") String entityId);
}
