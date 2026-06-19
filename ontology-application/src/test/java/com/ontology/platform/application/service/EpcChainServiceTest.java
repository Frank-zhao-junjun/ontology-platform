package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateEpcChainRequest;
import com.ontology.platform.application.dto.domain.EpcChainResponse;
import com.ontology.platform.infrastructure.persistence.EpcChainPO;
import com.ontology.platform.infrastructure.persistence.EpcChainPOMapper;
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
class EpcChainServiceTest {

    @Mock
    private EpcChainPOMapper mapper;

    @InjectMocks
    private EpcChainService epcChainService;

    @Test
    void create_shouldReturnResponse() {
        String id = UUID.randomUUID().toString();
        EpcChainPO po = EpcChainPO.builder().id(id).build();
        when(mapper.insert(any())).thenReturn(1);

        CreateEpcChainRequest req = CreateEpcChainRequest.builder().build();
        EpcChainResponse resp = epcChainService.create("onto-1", req, "test-user");

        assertNotNull(resp);
        verify(mapper).insert(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        String id = UUID.randomUUID().toString();
        EpcChainPO po = EpcChainPO.builder().id(id).build();
        when(mapper.selectById(id)).thenReturn(po);

        EpcChainResponse resp = epcChainService.getById(id);
        assertNotNull(resp);
        assertEquals(id, resp.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(mapper.selectById("nonexistent")).thenReturn(null);
        assertNull(epcChainService.getById("nonexistent"));
    }

    @Test
    void list_shouldReturnAll() {
        when(mapper.selectList(null)).thenReturn(List.of(
                EpcChainPO.builder().id("1").build(),
                EpcChainPO.builder().id("2").build()
        ));
        List<EpcChainResponse> list = epcChainService.list();
        assertEquals(2, list.size());
    }

    @Test
    void delete_shouldCallMapperDelete() {
        epcChainService.delete("some-id");
        verify(mapper).deleteById("some-id");
    }
}
