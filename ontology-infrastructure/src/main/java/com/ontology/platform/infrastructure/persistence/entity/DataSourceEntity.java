package com.ontology.platform.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

@Entity
@Table(name = "data_sources")
@Getter
@Setter
public class DataSourceEntity {
    @Id
    @Column(length = 36)
    private String id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 50, unique = true)
    private String code;
    @Column(name = "source_type", nullable = false, length = 20)
    private String sourceType;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "connection_config", columnDefinition = "json")
    private String connectionConfig = "{}";
    @Column(name = "credential_ref", length = 200)
    private String credentialRef;
    @Column(name = "is_active")
    private boolean active = true;
    @Column(name = "created_at")
    private Instant createdAt;
}
