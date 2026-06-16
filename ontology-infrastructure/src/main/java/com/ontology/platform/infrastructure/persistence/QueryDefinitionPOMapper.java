package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface QueryDefinitionPOMapper extends BaseMapper<QueryDefinitionPO> {
    List<QueryDefinitionPO> selectByOntologyId(@Param("ontologyId") String ontologyId);
}
