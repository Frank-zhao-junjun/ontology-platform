package com.ontology.platform.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.api.dto.BoundedContextCreateRequest;
import com.ontology.platform.api.dto.ReviewCommentRequest;
import com.ontology.platform.api.dto.WorkflowTransitionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles(value = "h2", inheritProfiles = false)
class BoundedContextControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void shouldCreateBoundedContext() throws Exception {
        BoundedContextCreateRequest req = new BoundedContextCreateRequest();
        req.setName("test");
        req.setCode("testctx");
        req.setDomainTag("manufacturing");
        String resp = mockMvc.perform(post("/v1/contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        System.out.println("RESPONSE: " + resp);
    }

    @Test
    void shouldSubmitForReviewAndLogStateChange() throws Exception {
        // 1. 创建上下文
        BoundedContextCreateRequest req = new BoundedContextCreateRequest();
        req.setName("workflow-test-1");
        req.setCode("wftest1");
        req.setDomainTag("manufacturing");
        String createResp = mockMvc.perform(post("/v1/contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ctxId = objectMapper.readTree(createResp).get("data").get("id").asText();

        // 2. 提交审核
        WorkflowTransitionRequest transReq = new WorkflowTransitionRequest();
        transReq.setOperatedBy("litest");
        transReq.setComment("建模完成，提交审核");
        mockMvc.perform(post("/v1/contexts/" + ctxId + "/submit-review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workflowState").value("IN_REVIEW"));

        // 3. 查询工作流日志
        mockMvc.perform(get("/v1/contexts/" + ctxId + "/workflow-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].fromState").value("DRAFT"))
                .andExpect(jsonPath("$.data[0].toState").value("IN_REVIEW"))
                .andExpect(jsonPath("$.data[0].operatedBy").value("litest"))
                .andExpect(jsonPath("$.data[0].comment").value("建模完成，提交审核"));
    }

    @Test
    void shouldApproveAndPublishWithAuditLog() throws Exception {
        // 1. 创建上下文
        BoundedContextCreateRequest req = new BoundedContextCreateRequest();
        req.setName("workflow-test-2");
        req.setCode("wftest2");
        req.setDomainTag("manufacturing");
        String createResp = mockMvc.perform(post("/v1/contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ctxId = objectMapper.readTree(createResp).get("data").get("id").asText();

        // 2. 提交审核
        mockMvc.perform(post("/v1/contexts/" + ctxId + "/submit-review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new WorkflowTransitionRequest())))
                .andExpect(status().isOk());

        // 3. 批准发布
        WorkflowTransitionRequest approveReq = new WorkflowTransitionRequest();
        approveReq.setOperatedBy("admin");
        approveReq.setComment("审核通过，批准发布");
        mockMvc.perform(post("/v1/contexts/" + ctxId + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.context.workflowState").value("PUBLISHED"))
                .andExpect(jsonPath("$.data.publishedManifest").exists());

        // 4. 查询工作流日志（应有2条记录）
        mockMvc.perform(get("/v1/contexts/" + ctxId + "/workflow-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].toState").value("PUBLISHED"))
                .andExpect(jsonPath("$.data[1].toState").value("IN_REVIEW"));
    }

    @Test
    void shouldRejectToDraftWithAuditLog() throws Exception {
        // 1. 创建并提交审核
        BoundedContextCreateRequest req = new BoundedContextCreateRequest();
        req.setName("workflow-test-3");
        req.setCode("wftest3");
        req.setDomainTag("manufacturing");
        String createResp = mockMvc.perform(post("/v1/contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ctxId = objectMapper.readTree(createResp).get("data").get("id").asText();

        mockMvc.perform(post("/v1/contexts/" + ctxId + "/submit-review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new WorkflowTransitionRequest())))
                .andExpect(status().isOk());

        // 2. 拒绝退回
        WorkflowTransitionRequest rejectReq = new WorkflowTransitionRequest();
        rejectReq.setOperatedBy("reviewer");
        rejectReq.setComment("需补充校验规则");
        mockMvc.perform(post("/v1/contexts/" + ctxId + "/reject-review")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.workflowState").value("DRAFT"));
    }

    @Test
    void shouldCreateAndListReviewComments() throws Exception {
        // 1. 创建上下文
        BoundedContextCreateRequest req = new BoundedContextCreateRequest();
        req.setName("review-test");
        req.setCode("reviewtest");
        req.setDomainTag("manufacturing");
        String createResp = mockMvc.perform(post("/v1/contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ctxId = objectMapper.readTree(createResp).get("data").get("id").asText();

        // 2. 创建审核批注
        ReviewCommentRequest commentReq = new ReviewCommentRequest();
        commentReq.setTargetType("BEHAVIOR");
        commentReq.setTargetId("bhv-fake-id");
        commentReq.setReviewer("reviewer1");
        commentReq.setResolution("NEEDS_CHANGE");
        commentReq.setContent("生产订单下达行为需增加参数校验");
        mockMvc.perform(post("/v1/contexts/" + ctxId + "/review-comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.targetType").value("BEHAVIOR"))
                .andExpect(jsonPath("$.data.resolution").value("NEEDS_CHANGE"));

        // 3. 查询批注列表
        mockMvc.perform(get("/v1/contexts/" + ctxId + "/review-comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].reviewer").value("reviewer1"));
    }

    @Test
    void shouldResolveReviewComment() throws Exception {
        // 1. 创建上下文和批注
        BoundedContextCreateRequest req = new BoundedContextCreateRequest();
        req.setName("resolve-test");
        req.setCode("resolvetest");
        req.setDomainTag("manufacturing");
        String createResp = mockMvc.perform(post("/v1/contexts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String ctxId = objectMapper.readTree(createResp).get("data").get("id").asText();

        ReviewCommentRequest commentReq = new ReviewCommentRequest();
        commentReq.setTargetType("RULE");
        commentReq.setTargetId("rule-fake-id");
        commentReq.setReviewer("reviewer1");
        commentReq.setContent("测试批注");
        String commentResp = mockMvc.perform(post("/v1/contexts/" + ctxId + "/review-comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String commentId = objectMapper.readTree(commentResp).get("data").get("id").asText();

        // 2. 更新决议
        mockMvc.perform(put("/v1/contexts/" + ctxId + "/review-comments/" + commentId + "/resolve")
                .param("resolution", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resolution").value("APPROVED"))
                .andExpect(jsonPath("$.data.resolvedAt").exists());
    }
}
