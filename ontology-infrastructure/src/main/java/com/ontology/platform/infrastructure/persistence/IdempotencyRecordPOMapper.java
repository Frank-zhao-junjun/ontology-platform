package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IdempotencyRecordPOMapper extends BaseMapper<IdempotencyRecordPO> {
    List<IdempotencyRecordPO> selectExpired(@Param("before") java.time.Instant before);
    int deleteExpired(@Param("before") java.time.Instant before);
}
