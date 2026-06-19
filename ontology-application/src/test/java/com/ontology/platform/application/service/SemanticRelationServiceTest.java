package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateSemanticRelationRequest;
import com.ontology.platform.application.dto.domain.SemanticRelationResponse;
import com.ontology.platform.infrastructure.persistence.SemanticRelationPO;
import com.ontology.platform.infrastructure.persistence.SemanticRelationPOMapper;
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
class SemanticRelationServiceTest {

    @Mock
    private SemanticRelationPOMapper mapper;

    @InjectMocks
    private SemanticRelationService semanticRelationService;

    @Test
    void create_shouldReturnResponse() {
        String id = UUID.randomUUID().toString();
        SemanticRelationPO po = SemanticRelationPO.builder().id(id).build();
        when(mapper.insert(any())).thenReturn(1);

        CreateSemanticRelationRequest req = CreateSemanticRelationRequest.builder().build();
        SemanticRelationResponse resp = semanticRelationService.create("onto-1", req, "test-user");

        assertNotNull(resp);
        verify(mapper).insert(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        String id = UUID.randomUUID().toString();
        SemanticRelationPO po = SemanticRelationPO.builder().id(id).build();
        when(mapper.selectById(id)).thenReturn(po);

        SemanticRelationResponse resp = semanticRelationService.getById(id);
        assertNotNull(resp);
        assertEquals(id, resp.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(mapper.selectById("nonexistent")).thenReturn(null);
        assertNull(semanticRelationService.getById("nonexistent"));
    }

    @Test
    void list_shouldReturnAll() {
        when(mapper.selectList(null)).thenReturn(List.of(
                SemanticRelationPO.builder().id("1").build(),
                SemanticRelationPO.builder().id("2").build()
        ));
        List<SemanticRelationResponse> list = semanticRelationService.list();
        assertEquals(2, list.size());
    }

    @Test
    void delete_shouldCallMapperDelete() {
        semanticRelationService.delete("some-id");
        verify(mapper).deleteById("some-id");
    }
}
