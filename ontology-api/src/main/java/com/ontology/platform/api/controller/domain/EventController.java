package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.EventDefinitionResponse;
import com.ontology.platform.application.service.DomainQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ontologies/{ontologyId}/events")
@RequiredArgsConstructor
@Tag(name = "事件层", description = "领域事件查询 API (P05)")
public class EventController {

    private final DomainQueryService domainQueryService;

    @GetMapping
    @Operation(summary = "查询领域事件列表", description = "按本体ID查询领域事件，可按实体过滤")
    public ApiResponse<List<EventDefinitionResponse>> queryEvents(
            @PathVariable String ontologyId,
            @Parameter(description = "实体ID过滤") @RequestParam(required = false) String entityId) {
        List<EventDefinitionResponse> events = domainQueryService.queryEvents(ontologyId, entityId);
        return ApiResponse.success(events);
    }
}
