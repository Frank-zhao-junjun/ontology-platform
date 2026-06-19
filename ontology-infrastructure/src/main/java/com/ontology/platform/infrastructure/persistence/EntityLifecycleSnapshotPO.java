package com.ontology.platform.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@TableName("entity_lifecycle_snapshot")
public class EntityLifecycleSnapshotPO {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    @TableField("entity_id")
    private String entityId;
    @TableField("ontology_id")
    private String ontologyId;
    @TableField("lifecycle_data")
    private String lifecycleData;
    @TableField("snapshot_version")
    private String snapshotVersion;
    @TableField("created_at")
    private Instant createdAt;

}
