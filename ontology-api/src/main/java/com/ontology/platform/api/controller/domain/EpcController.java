package com.ontology.platform.api.controller.domain;

import com.ontology.platform.api.dto.ApiResponse;
import com.ontology.platform.application.dto.domain.EpcStepResponse;
import com.ontology.platform.application.service.DomainQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ontologies/{ontologyId}/epc")
@RequiredArgsConstructor
@Tag(name = "编排层", description = "EPC 编排步骤查询 API (P06)")
public class EpcController {

    private final DomainQueryService domainQueryService;

    @GetMapping
    @Operation(summary = "查询EPC步骤列表", description = "按本体ID查询EPC步骤，可按流程名过滤")
    public ApiResponse<List<EpcStepResponse>> queryEpc(
            @Parameter(description = "本体ID") @PathVariable String ontologyId,
            @Parameter(description = "流程名过滤") @RequestParam(required = false) String flowName) {
        List<EpcStepResponse> steps = domainQueryService.queryEpc(ontologyId, flowName);
        return ApiResponse.success(steps);
    }
}
