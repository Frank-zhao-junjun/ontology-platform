package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DomainEventPOMapper extends BaseMapper<DomainEventPO> {
    List<DomainEventPO> selectByOntologyId(@Param("ontologyId") String ontologyId);
    List<DomainEventPO> selectByOntologyIdAndEntityId(@Param("ontologyId") String ontologyId, @Param("entityId") String entityId);
    List<DomainEventPO> selectByOntologyIdAndEventType(@Param("ontologyId") String ontologyId, @Param("eventType") String eventType);
}
