package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity
@Table(name = "data_access_methods",
        uniqueConstraints = @UniqueConstraint(columnNames = {"object_type_id", "data_source_id", "method_type"}))
@Getter
@Setter
public class DataAccessMethodEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(name = "context_id", length = 36)
    private String contextId;
    @Column(name = "object_type_id", nullable = false, length = 36)
    private String objectTypeId;
    @Column(name = "data_source_id", nullable = false, length = 36)
    private String dataSourceId;
    @Column(name = "method_type", nullable = false, length = 20)
    private String methodType;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "access_config", columnDefinition = "json")
    private String accessConfig = "{}";
    @Column(name = "cache_ttl_sec")
    private int cacheTtlSec = 300;
    @Column(name = "created_at")
    private Instant createdAt;
}
