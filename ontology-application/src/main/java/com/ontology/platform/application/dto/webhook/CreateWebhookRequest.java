package com.ontology.platform.application.dto.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建Webhook请求DTO")
public class CreateWebhookRequest {

    @NotBlank(message = "callbackUrl不能为空")
    private String callbackUrl;

    @Builder.Default
    private List<String> eventTypes = List.of("job.completed", "job.failed");

    @NotBlank(message = "secret不能为空")
    private String secret;
}
