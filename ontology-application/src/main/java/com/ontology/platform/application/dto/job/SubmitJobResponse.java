package com.ontology.platform.application.dto.job;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitJobResponse {

    private String jobId;
    private String jobType;
    private String status;
    private Instant createdAt;
}
