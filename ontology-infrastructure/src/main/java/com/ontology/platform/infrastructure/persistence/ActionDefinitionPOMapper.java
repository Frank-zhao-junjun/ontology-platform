package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ActionDefinitionPOMapper extends BaseMapper<ActionDefinitionPO> {
    List<ActionDefinitionPO> selectByOntologyId(@Param("ontologyId") String ontologyId);
    List<ActionDefinitionPO> selectByOntologyIdAndEntityId(@Param("ontologyId") String ontologyId, @Param("entityId") String entityId);
    List<ActionDefinitionPO> selectByOntologyIdAndDomain(@Param("ontologyId") String ontologyId, @Param("domain") String domain);
}
