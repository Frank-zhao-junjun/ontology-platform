package com.ontology.platform.api.controller.semantic;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.service.semantic.SemanticService;
import com.ontology.platform.domain.dto.semantic.IntentResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v2/semantic")
@RequiredArgsConstructor
@Tag(name = "Semantic", description = "Phase 3c 语义服务API")
public class SemanticController {

    private final SemanticService semanticService;

    @PostMapping("/resolve-intent")
    @Operation(summary = "解析意图", description = "根据 ontologyId + 自然语言短语匹配 intent（triggerPhrases）")
    public ResponseEntity<ApiResponse<IntentResult>> resolveIntent(
            @RequestBody Map<String, String> body) {

        String ontologyId = body != null ? body.get("ontologyId") : null;
        String phrase = body != null ? body.get("phrase") : null;
        if (phrase == null && body != null) {
            phrase = body.get("query");
        }
        log.info("REST: resolveIntent, ontologyId={}, phrase={}", ontologyId, phrase);

        IntentResult result = semanticService.resolveIntent(ontologyId, phrase);
        if (result == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "No matching intent found for phrase: " + phrase));
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
