package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CausalityPOMapper extends BaseMapper<CausalityPO> {
    List<CausalityPO> selectByOntologyId(@Param("ontologyId") String ontologyId);
    List<CausalityPO> selectByCauseEventId(@Param("causeEventId") String causeEventId);
    List<CausalityPO> selectByEffectEventId(@Param("effectEventId") String effectEventId);
}
