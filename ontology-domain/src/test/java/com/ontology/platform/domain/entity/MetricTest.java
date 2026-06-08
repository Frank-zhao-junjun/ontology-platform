package com.ontology.platform.domain.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetricTest {

    @Test
    void createShouldGenerateIdAndDefaults() {
        Metric m = Metric.create(
                "ctx-1", "ontime_completion_rate", "准时完工率",
                "OnTime Completion Rate", "按时完工工单数 / 总完工工单数 * 100%",
                "[{\"eventId\":\"work_order_tech_close\",\"field\":\"timestamp\"}]",
                "[\"workshop\",\"month\"]", "monthly"
        );

        assertThat(m.getId()).isNotNull();
        assertThat(m.getId()).hasSize(36);
        assertThat(m.getContextId()).isEqualTo("ctx-1");
        assertThat(m.getManifestCode()).isEqualTo("ontime_completion_rate");
        assertThat(m.getName()).isEqualTo("准时完工率");
        assertThat(m.getNameEn()).isEqualTo("OnTime Completion Rate");
        assertThat(m.getFormula()).contains("按时完工");
        assertThat(m.getDataSourceRefJson()).contains("work_order_tech_close");
        assertThat(m.getAggregationDimensionsJson()).contains("workshop");
        assertThat(m.getPeriod()).isEqualTo("monthly");
        assertThat(m.getCreatedAt()).isNotNull();
    }

    @Test
    void createShouldAcceptEmptyJsonFields() {
        Metric m = Metric.create("ctx-1", "avg_completion_time", "工单平均完成时间",
                "Avg Completion Time", "SUM(completion_time - dispatch_time) / count",
                null, null, null);

        assertThat(m.getDataSourceRefJson()).isEqualTo("[]");
        assertThat(m.getAggregationDimensionsJson()).isEqualTo("[]");
        assertThat(m.getPeriod()).isNull();
    }

    @Test
    void builderShouldAllowIdOverride() {
        Metric m = Metric.builder().id("custom-id").contextId("ctx-1")
                .manifestCode("test").name("Test").formula("1+1").build();

        assertThat(m.getId()).isEqualTo("custom-id");
    }
}
