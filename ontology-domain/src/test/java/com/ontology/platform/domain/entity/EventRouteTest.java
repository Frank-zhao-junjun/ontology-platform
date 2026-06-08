package com.ontology.platform.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventRouteTest {

    @Test
    void createShouldSetDefaults() {
        EventRoute r = EventRoute.create("ctx-1", "route_prod_order_created",
                "evt-1", "[{\"type\":\"BOUNDED_CONTEXT\",\"targetId\":\"ctx-mat\"}]",
                "[{\"field\":\"scenario\",\"op\":\"EQ\",\"value\":\"MTO\"}]");

        assertThat(r.getId()).isNotBlank();
        assertThat(r.getContextId()).isEqualTo("ctx-1");
        assertThat(r.getManifestCode()).isEqualTo("route_prod_order_created");
        assertThat(r.getSourceEventId()).isEqualTo("evt-1");
        assertThat(r.getRouteTargetsJson()).contains("BOUNDED_CONTEXT");
        assertThat(r.getFilterConditionsJson()).contains("MTO");
        assertThat(r.getCreatedAt()).isNotNull();
    }

    @Test
    void createShouldDefaultEmptyJsons() {
        EventRoute r = EventRoute.create("ctx-1", "route_simple", "evt-1", null, null);

        assertThat(r.getRouteTargetsJson()).isEqualTo("[]");
        assertThat(r.getFilterConditionsJson()).isEqualTo("[]");
    }

    @Test
    void builderShouldAllowCustomId() {
        EventRoute r = EventRoute.builder().id("custom-id").contextId("ctx-1")
                .manifestCode("r1").sourceEventId("evt-1").routeTargetsJson("[]").build();

        assertThat(r.getId()).isEqualTo("custom-id");
    }
}
