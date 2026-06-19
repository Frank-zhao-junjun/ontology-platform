package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateSemanticFieldMappingRequest;
import com.ontology.platform.application.dto.domain.SemanticFieldMappingResponse;
import com.ontology.platform.infrastructure.persistence.SemanticFieldMappingPO;
import com.ontology.platform.infrastructure.persistence.SemanticFieldMappingPOMapper;
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
class SemanticFieldMappingServiceTest {

    @Mock
    private SemanticFieldMappingPOMapper mapper;

    @InjectMocks
    private SemanticFieldMappingService semanticFieldMappingService;

    @Test
    void create_shouldReturnResponse() {
        String id = UUID.randomUUID().toString();
        SemanticFieldMappingPO po = SemanticFieldMappingPO.builder().id(id).build();
        when(mapper.insert(any())).thenReturn(1);

        CreateSemanticFieldMappingRequest req = CreateSemanticFieldMappingRequest.builder().build();
        SemanticFieldMappingResponse resp = semanticFieldMappingService.create("onto-1", req, "test-user");

        assertNotNull(resp);
        verify(mapper).insert(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        String id = UUID.randomUUID().toString();
        SemanticFieldMappingPO po = SemanticFieldMappingPO.builder().id(id).build();
        when(mapper.selectById(id)).thenReturn(po);

        SemanticFieldMappingResponse resp = semanticFieldMappingService.getById(id);
        assertNotNull(resp);
        assertEquals(id, resp.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(mapper.selectById("nonexistent")).thenReturn(null);
        assertNull(semanticFieldMappingService.getById("nonexistent"));
    }

    @Test
    void list_shouldReturnAll() {
        when(mapper.selectList(null)).thenReturn(List.of(
                SemanticFieldMappingPO.builder().id("1").build(),
                SemanticFieldMappingPO.builder().id("2").build()
        ));
        List<SemanticFieldMappingResponse> list = semanticFieldMappingService.list();
        assertEquals(2, list.size());
    }

    @Test
    void delete_shouldCallMapperDelete() {
        semanticFieldMappingService.delete("some-id");
        verify(mapper).deleteById("some-id");
    }
}
