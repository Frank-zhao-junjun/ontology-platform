package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateAgentPolicySemanticRequest;
import com.ontology.platform.application.dto.domain.AgentPolicySemanticResponse;
import com.ontology.platform.infrastructure.persistence.AgentPolicySemanticPO;
import com.ontology.platform.infrastructure.persistence.AgentPolicySemanticPOMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentPolicySemanticServiceTest {

    @Mock
    private AgentPolicySemanticPOMapper mapper;

    @InjectMocks
    private AgentPolicySemanticService agentPolicySemanticService;

    @Test
    void create_shouldReturnResponse() {
        String id = UUID.randomUUID().toString();
        AgentPolicySemanticPO po = AgentPolicySemanticPO.builder().id(id).build();
        when(mapper.insert(any())).thenReturn(1);

        CreateAgentPolicySemanticRequest req = CreateAgentPolicySemanticRequest.builder().build();
        AgentPolicySemanticResponse resp = agentPolicySemanticService.create("onto-1", req, "test-user");

        assertNotNull(resp);
        verify(mapper).insert(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        String id = UUID.randomUUID().toString();
        AgentPolicySemanticPO po = AgentPolicySemanticPO.builder().id(id).build();
        when(mapper.selectById(id)).thenReturn(po);

        AgentPolicySemanticResponse resp = agentPolicySemanticService.getById(id);
        assertNotNull(resp);
        assertEquals(id, resp.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(mapper.selectById("nonexistent")).thenReturn(null);
        assertNull(agentPolicySemanticService.getById("nonexistent"));
    }

    @Test
    void list_shouldReturnAll() {
        when(mapper.selectList(null)).thenReturn(List.of(
                AgentPolicySemanticPO.builder().id("1").build(),
                AgentPolicySemanticPO.builder().id("2").build()
        ));
        List<AgentPolicySemanticResponse> list = agentPolicySemanticService.list();
        assertEquals(2, list.size());
    }

    @Test
    void delete_shouldCallMapperDelete() {
        agentPolicySemanticService.delete("some-id");
        verify(mapper).deleteById("some-id");
    }
}
