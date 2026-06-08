package com.ontology.platform.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventHandlerTest {

    @Test
    void createShouldSetDefaults() {
        EventHandler h = EventHandler.create("ctx-1", "handler_on_create_notify",
                "evt-1", "action-notify-workshop", "SCI-MTO", "ISSUED", 50, "ASYNC");

        assertThat(h.getId()).isNotBlank();
        assertThat(h.getContextId()).isEqualTo("ctx-1");
        assertThat(h.getManifestCode()).isEqualTo("handler_on_create_notify");
        assertThat(h.getEventId()).isEqualTo("evt-1");
        assertThat(h.getHandlerBehaviorId()).isEqualTo("action-notify-workshop");
        assertThat(h.getScenarioId()).isEqualTo("SCI-MTO");
        assertThat(h.getPreconditionState()).isEqualTo("ISSUED");
        assertThat(h.getPriority()).isEqualTo(50);
        assertThat(h.getExecutionMode()).isEqualTo("ASYNC");
        assertThat(h.getCreatedAt()).isNotNull();
    }

    @Test
    void priorityShouldDefaultTo100WhenZero() {
        EventHandler h = EventHandler.create("ctx-1", "h1", "evt-1",
                "action-1", null, null, 0, "SYNC");

        assertThat(h.getPriority()).isEqualTo(100);
    }

    @Test
    void executionModeDefaultsToSync() {
        EventHandler h = EventHandler.create("ctx-1", "h1", "evt-1",
                "action-1", null, null, 10, null);

        assertThat(h.getExecutionMode()).isEqualTo("SYNC");
    }

    @Test
    void builderShouldAllowCustomId() {
        EventHandler h = EventHandler.builder().id("custom-id").contextId("ctx-1")
                .manifestCode("h1").eventId("evt-1").handlerBehaviorId("action-1")
                .scenarioId(null).preconditionState(null).priority(10)
                .executionMode("SYNC").build();

        assertThat(h.getId()).isEqualTo("custom-id");
    }
}
