package com.ontology.platform.domain.entity;

import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
public class DataAccessMethod {
    private final String id;
    private final String contextId;
    private final String objectTypeId;
    private final String dataSourceId;
    private final String methodType;
    private final String accessConfig;
    private final int cacheTtlSec;
    private final Instant createdAt;

    private DataAccessMethod(String id, String contextId, String objectTypeId, String dataSourceId,
                             String methodType, String accessConfig, int cacheTtlSec) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.contextId = contextId;
        this.objectTypeId = objectTypeId;
        this.dataSourceId = dataSourceId;
        this.methodType = methodType;
        this.accessConfig = accessConfig != null ? accessConfig : "{}";
        this.cacheTtlSec = cacheTtlSec > 0 ? cacheTtlSec : 300;
        this.createdAt = Instant.now();
    }

    public static DataAccessMethod create(String contextId, String objectTypeId, String dataSourceId,
                                          String methodType, String accessConfig, int cacheTtlSec) {
        return new DataAccessMethod(null, contextId, objectTypeId, dataSourceId, methodType, accessConfig, cacheTtlSec);
    }
}
