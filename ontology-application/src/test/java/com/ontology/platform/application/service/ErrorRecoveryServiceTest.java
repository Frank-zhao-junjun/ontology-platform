package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateErrorRecoveryRequest;
import com.ontology.platform.application.dto.domain.ErrorRecoveryResponse;
import com.ontology.platform.infrastructure.persistence.ErrorRecoveryPO;
import com.ontology.platform.infrastructure.persistence.ErrorRecoveryPOMapper;
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
class ErrorRecoveryServiceTest {

    @Mock
    private ErrorRecoveryPOMapper mapper;

    @InjectMocks
    private ErrorRecoveryService errorRecoveryService;

    @Test
    void create_shouldReturnResponse() {
        String id = UUID.randomUUID().toString();
        ErrorRecoveryPO po = ErrorRecoveryPO.builder().id(id).build();
        when(mapper.insert(any())).thenReturn(1);

        CreateErrorRecoveryRequest req = CreateErrorRecoveryRequest.builder().build();
        ErrorRecoveryResponse resp = errorRecoveryService.create("onto-1", req, "test-user");

        assertNotNull(resp);
        verify(mapper).insert(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        String id = UUID.randomUUID().toString();
        ErrorRecoveryPO po = ErrorRecoveryPO.builder().id(id).build();
        when(mapper.selectById(id)).thenReturn(po);

        ErrorRecoveryResponse resp = errorRecoveryService.getById(id);
        assertNotNull(resp);
        assertEquals(id, resp.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(mapper.selectById("nonexistent")).thenReturn(null);
        assertNull(errorRecoveryService.getById("nonexistent"));
    }

    @Test
    void list_shouldReturnAll() {
        when(mapper.selectList(null)).thenReturn(List.of(
                ErrorRecoveryPO.builder().id("1").build(),
                ErrorRecoveryPO.builder().id("2").build()
        ));
        List<ErrorRecoveryResponse> list = errorRecoveryService.list();
        assertEquals(2, list.size());
    }

    @Test
    void delete_shouldCallMapperDelete() {
        errorRecoveryService.delete("some-id");
        verify(mapper).deleteById("some-id");
    }
}
