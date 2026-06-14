package com.ontology.platform.application.service.job;

import java.util.Map;

/**
 * Handler for async job execution. One implementation per job type.
 */
public interface JobHandler {

    /** Job type this handler supports (e.g. "import.execute") */
    String supportedJobType();

    /**
     * Execute the job synchronously.
     *
     * @param payload   job payload from the enqueue request
     * @param tenantId  tenant context
     * @param agentId   agent that submitted the job
     * @return result map (will be stored as JSONB), or null
     * @throws Exception on failure (will trigger retry/fail logic)
     */
    Map<String, Object> execute(Map<String, Object> payload, String tenantId, String agentId) throws Exception;
}
