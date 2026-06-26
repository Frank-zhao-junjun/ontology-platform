package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface GovernanceRolePOMapper extends BaseMapper<GovernanceRolePO> {
    @Select("SELECT * FROM governance_role WHERE ontology_id = #{ontologyId}")
    List<GovernanceRolePO> selectByOntologyId(String ontologyId);
}
