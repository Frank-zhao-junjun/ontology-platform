package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.ActionDefinitionResponse;
import com.ontology.platform.application.service.DomainQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ontologies/{ontologyId}/actions")
@RequiredArgsConstructor
@Tag(name = "行为层", description = "行为/动作定义查询 API (P04)")
public class BehaviorController {

    private final DomainQueryService domainQueryService;

    @GetMapping
    @Operation(summary = "查询行为定义列表", description = "按本体ID查询行为定义，可按实体过滤")
    public ApiResponse<List<ActionDefinitionResponse>> queryActions(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "实体ID过滤") @RequestParam(required = false) String entityId) {
        List<ActionDefinitionResponse> actions = domainQueryService.queryActions(ontologyId, entityId);
        return ApiResponse.success(actions);
    }
}
