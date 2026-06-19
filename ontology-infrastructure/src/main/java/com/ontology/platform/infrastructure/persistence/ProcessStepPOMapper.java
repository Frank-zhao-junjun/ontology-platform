package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProcessStepPOMapper extends BaseMapper<ProcessStepPO> {
    List<ProcessStepPO> selectByOntologyId(@Param("ontologyId") String ontologyId);
}
