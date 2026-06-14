package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobRecordPOMapper extends BaseMapper<JobRecordPO> {

    List<JobRecordPO> selectByStatus(@Param("status") String status,
                                     @Param("tenantId") String tenantId,
                                     @Param("limit") int limit);

    int updateStatus(@Param("id") java.util.UUID id,
                     @Param("status") String status,
                     @Param("errorMessage") String errorMessage);
}
