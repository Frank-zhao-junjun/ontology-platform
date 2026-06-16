package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProbeDefinitionPOMapper extends BaseMapper<ProbeDefinitionPO> {
    List<ProbeDefinitionPO> selectByOntologyId(@Param("ontologyId") String ontologyId);
}
