package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateEpcModelRefRequest;
import com.ontology.platform.application.dto.domain.EpcModelRefResponse;
import com.ontology.platform.infrastructure.persistence.EpcModelRefPO;
import com.ontology.platform.infrastructure.persistence.EpcModelRefPOMapper;
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
class EpcModelRefServiceTest {

    @Mock
    private EpcModelRefPOMapper mapper;

    @InjectMocks
    private EpcModelRefService epcModelRefService;

    @Test
    void create_shouldReturnResponse() {
        String id = UUID.randomUUID().toString();
        EpcModelRefPO po = EpcModelRefPO.builder().id(id).build();
        when(mapper.insert(any())).thenReturn(1);

        CreateEpcModelRefRequest req = CreateEpcModelRefRequest.builder().build();
        EpcModelRefResponse resp = epcModelRefService.create("onto-1", req, "test-user");

        assertNotNull(resp);
        verify(mapper).insert(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        String id = UUID.randomUUID().toString();
        EpcModelRefPO po = EpcModelRefPO.builder().id(id).build();
        when(mapper.selectById(id)).thenReturn(po);

        EpcModelRefResponse resp = epcModelRefService.getById(id);
        assertNotNull(resp);
        assertEquals(id, resp.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(mapper.selectById("nonexistent")).thenReturn(null);
        assertNull(epcModelRefService.getById("nonexistent"));
    }

    @Test
    void list_shouldReturnAll() {
        when(mapper.selectList(null)).thenReturn(List.of(
                EpcModelRefPO.builder().id("1").build(),
                EpcModelRefPO.builder().id("2").build()
        ));
        List<EpcModelRefResponse> list = epcModelRefService.list();
        assertEquals(2, list.size());
    }

    @Test
    void delete_shouldCallMapperDelete() {
        epcModelRefService.delete("some-id");
        verify(mapper).deleteById("some-id");
    }
}
