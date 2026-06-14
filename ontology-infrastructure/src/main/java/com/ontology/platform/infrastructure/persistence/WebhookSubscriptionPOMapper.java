package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WebhookSubscriptionPOMapper extends BaseMapper<WebhookSubscriptionPO> {

    List<WebhookSubscriptionPO> selectActiveByTenant(@Param("tenantId") String tenantId);

    List<WebhookSubscriptionPO> selectByEventType(@Param("eventType") String eventType);
}
