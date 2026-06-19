package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateEpcNodeRequest;
import com.ontology.platform.application.dto.domain.EpcNodeResponse;
import com.ontology.platform.infrastructure.persistence.EpcNodePO;
import com.ontology.platform.infrastructure.persistence.EpcNodePOMapper;
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
class EpcNodeServiceTest {

    @Mock
    private EpcNodePOMapper mapper;

    @InjectMocks
    private EpcNodeService epcNodeService;

    @Test
    void create_shouldReturnResponse() {
        String id = UUID.randomUUID().toString();
        EpcNodePO po = EpcNodePO.builder().id(id).build();
        when(mapper.insert(any())).thenReturn(1);

        CreateEpcNodeRequest req = CreateEpcNodeRequest.builder().build();
        EpcNodeResponse resp = epcNodeService.create("onto-1", req, "test-user");

        assertNotNull(resp);
        verify(mapper).insert(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        String id = UUID.randomUUID().toString();
        EpcNodePO po = EpcNodePO.builder().id(id).build();
        when(mapper.selectById(id)).thenReturn(po);

        EpcNodeResponse resp = epcNodeService.getById(id);
        assertNotNull(resp);
        assertEquals(id, resp.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(mapper.selectById("nonexistent")).thenReturn(null);
        assertNull(epcNodeService.getById("nonexistent"));
    }

    @Test
    void list_shouldReturnAll() {
        when(mapper.selectList(null)).thenReturn(List.of(
                EpcNodePO.builder().id("1").build(),
                EpcNodePO.builder().id("2").build()
        ));
        List<EpcNodeResponse> list = epcNodeService.list();
        assertEquals(2, list.size());
    }

    @Test
    void delete_shouldCallMapperDelete() {
        epcNodeService.delete("some-id");
        verify(mapper).deleteById("some-id");
    }
}
