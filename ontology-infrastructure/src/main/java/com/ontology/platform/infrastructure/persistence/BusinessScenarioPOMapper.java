package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业务场景持久化Mapper接口
 * BusinessScenario Persistence Mapper Interface
 */
@Mapper
public interface BusinessScenarioPOMapper extends BaseMapper<BusinessScenarioPO> {

    List<BusinessScenarioPO> selectByOntologyId(@Param("ontologyId") String ontologyId);
}
