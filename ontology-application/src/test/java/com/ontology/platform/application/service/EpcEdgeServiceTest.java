package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateEpcEdgeRequest;
import com.ontology.platform.application.dto.domain.EpcEdgeResponse;
import com.ontology.platform.infrastructure.persistence.EpcEdgePO;
import com.ontology.platform.infrastructure.persistence.EpcEdgePOMapper;
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
class EpcEdgeServiceTest {

    @Mock
    private EpcEdgePOMapper mapper;

    @InjectMocks
    private EpcEdgeService epcEdgeService;

    @Test
    void create_shouldReturnResponse() {
        String id = UUID.randomUUID().toString();
        EpcEdgePO po = EpcEdgePO.builder().id(id).build();
        when(mapper.insert(any())).thenReturn(1);

        CreateEpcEdgeRequest req = CreateEpcEdgeRequest.builder().build();
        EpcEdgeResponse resp = epcEdgeService.create("onto-1", req, "test-user");

        assertNotNull(resp);
        verify(mapper).insert(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        String id = UUID.randomUUID().toString();
        EpcEdgePO po = EpcEdgePO.builder().id(id).build();
        when(mapper.selectById(id)).thenReturn(po);

        EpcEdgeResponse resp = epcEdgeService.getById(id);
        assertNotNull(resp);
        assertEquals(id, resp.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(mapper.selectById("nonexistent")).thenReturn(null);
        assertNull(epcEdgeService.getById("nonexistent"));
    }

    @Test
    void list_shouldReturnAll() {
        when(mapper.selectList(null)).thenReturn(List.of(
                EpcEdgePO.builder().id("1").build(),
                EpcEdgePO.builder().id("2").build()
        ));
        List<EpcEdgeResponse> list = epcEdgeService.list();
        assertEquals(2, list.size());
    }

    @Test
    void delete_shouldCallMapperDelete() {
        epcEdgeService.delete("some-id");
        verify(mapper).deleteById("some-id");
    }
}
