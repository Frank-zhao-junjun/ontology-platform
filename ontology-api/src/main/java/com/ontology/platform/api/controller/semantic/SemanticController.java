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

/**
 * REST controller for Phase 3c semantic operations including intent resolution.
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/semantic")
@RequiredArgsConstructor
@Tag(name = "Semantic", description = "Phase 3c 语义服务API")
public class SemanticController {

    private final SemanticService semanticService;

    /**
     * Resolve an intent from a natural language phrase.
     * Matches the phrase against stored intents' triggerPhrases.
     *
     * @param body request body containing the "phrase" field
     * @return matched IntentResult or 404 if no match found
     */
    @PostMapping("/resolve-intent")
    @Operation(summary = "解析意图", description = "根据输入的自然语言短语匹配意图（triggerPhrases 匹配）")
    public ResponseEntity<ApiResponse<IntentResult>> resolveIntent(
            @RequestBody Map<String, String> body) {

        String phrase = body != null ? body.get("phrase") : null;
        log.info("REST: resolveIntent, phrase={}", phrase);

        IntentResult result = semanticService.resolveIntent(phrase);
        if (result == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "No matching intent found for phrase: " + phrase));
        }

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
