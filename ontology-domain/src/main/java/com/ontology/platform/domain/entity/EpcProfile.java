package com.ontology.platform.domain.entity;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class EpcProfile {
    private String id;
    private String chainId;
    private String profileData;
    private String profileVersion;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public static EpcProfile create() {
        return EpcProfile.builder().id(UUID.randomUUID().toString())
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
