package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 清单导入持久化Mapper接口
 * Manifest Import Persistence Mapper Interface
 */
@Mapper
public interface ManifestImportPOMapper extends BaseMapper<ManifestImportPO> {
}
