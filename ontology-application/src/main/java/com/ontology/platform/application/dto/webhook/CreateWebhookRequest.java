package com.ontology.platform.application.dto.webhook;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWebhookRequest {

    @NotBlank(message = "callbackUrl不能为空")
    private String callbackUrl;

    @Builder.Default
    private List<String> eventTypes = List.of("job.completed", "job.failed");

    @NotBlank(message = "secret不能为空")
    private String secret;
}
