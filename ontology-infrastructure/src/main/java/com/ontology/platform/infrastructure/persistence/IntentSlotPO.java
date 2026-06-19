package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("intent_slot")
public class IntentSlotPO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @TableField("intent_id")
    private String intentId;
    @TableField("name")
    private String name;
    @TableField("slot_type")
    private String slotType;
    @TableField("required")
    private Boolean required;
    @TableField("examples")
    private String examples;
    @TableField("created_at")
    private Instant createdAt;

}
