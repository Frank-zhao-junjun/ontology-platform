package com.ontology.platform.api.controller.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontology.platform.api.config.GlobalExceptionHandler;
import com.ontology.platform.application.dto.webhook.CreateWebhookRequest;
import com.ontology.platform.application.dto.webhook.WebhookResponse;
import com.ontology.platform.application.service.webhook.WebhookService;
import com.ontology.platform.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link WebhookController}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookController Test")
class WebhookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WebhookService webhookService;

    @InjectMocks
    private WebhookController webhookController;

    private ObjectMapper objectMapper;

    private static final UUID TEST_WEBHOOK_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(webhookController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Nested
    @DisplayName("POST /api/v1/webhooks - create webhook")
    class CreateWebhookTests {

        @Test
        @DisplayName("should return 201 with webhook details")
        void createWebhookSuccess() throws Exception {
            var request = CreateWebhookRequest.builder()
                    .callbackUrl("https://agent.example.com/webhook")
                    .eventTypes(List.of("job.completed", "job.failed"))
                    .secret("whsec_abc123")
                    .build();

            var response = WebhookResponse.builder()
                    .id(TEST_WEBHOOK_ID)
                    .tenantId("default")
                    .agentId("user-1")
                    .callbackUrl("https://agent.example.com/webhook")
                    .eventTypes(List.of("job.completed", "job.failed"))
                    .isActive(true)
                    .createdAt(Instant.now())
                    .build();

            when(webhookService.createWebhook(any(), eq("default"), eq("user-1")))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/webhooks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Tenant-Id", "default")
                            .header("X-User-Id", "user-1")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.id").value(TEST_WEBHOOK_ID.toString()))
                    .andExpect(jsonPath("$.data.callbackUrl").value("https://agent.example.com/webhook"))
                    .andExpect(jsonPath("$.data.active").value(true));

            verify(webhookService).createWebhook(any(), eq("default"), eq("user-1"));
        }

        @Test
        @DisplayName("should return 400 when callbackUrl is blank")
        void createWebhookInvalid() throws Exception {
            var request = CreateWebhookRequest.builder()
                    .callbackUrl("")
                    .secret("whsec_abc123")
                    .build();

            mockMvc.perform(post("/api/v1/webhooks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Tenant-Id", "default")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(is(not(0))));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/webhooks - list webhooks")
    class ListWebhooksTests {

        @Test
        @DisplayName("should return webhook list")
        void listWebhooksSuccess() throws Exception {
            var webhooks = List.of(
                    WebhookResponse.builder().id(TEST_WEBHOOK_ID)
                            .callbackUrl("https://agent1.example.com/hook").isActive(true).build(),
                    WebhookResponse.builder().id(UUID.randomUUID())
                            .callbackUrl("https://agent2.example.com/hook").isActive(true).build()
            );

            when(webhookService.listWebhooks("default")).thenReturn(webhooks);

            mockMvc.perform(get("/api/v1/webhooks")
                            .header("X-Tenant-Id", "default"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }

        @Test
        @DisplayName("should return empty list")
        void listWebhooksEmpty() throws Exception {
            when(webhookService.listWebhooks(anyString())).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/webhooks")
                            .header("X-Tenant-Id", "default"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/webhooks/{id} - delete webhook")
    class DeleteWebhookTests {

        @Test
        @DisplayName("should delete webhook successfully")
        void deleteWebhookSuccess() throws Exception {
            doNothing().when(webhookService).deleteWebhook(TEST_WEBHOOK_ID);

            mockMvc.perform(delete("/api/v1/webhooks/" + TEST_WEBHOOK_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));

            verify(webhookService).deleteWebhook(TEST_WEBHOOK_ID);
        }

        @Test
        @DisplayName("should return 404 when webhook not found")
        void deleteWebhookNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("Webhook", TEST_WEBHOOK_ID.toString()))
                    .when(webhookService).deleteWebhook(TEST_WEBHOOK_ID);

            mockMvc.perform(delete("/api/v1/webhooks/" + TEST_WEBHOOK_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value(is(not(0))));
        }
    }
}