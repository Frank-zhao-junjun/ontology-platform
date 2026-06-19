package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateIntentSlotRequest;
import com.ontology.platform.application.dto.domain.IntentSlotResponse;
import com.ontology.platform.infrastructure.persistence.IntentSlotPO;
import com.ontology.platform.infrastructure.persistence.IntentSlotPOMapper;
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
class IntentSlotServiceTest {

    @Mock
    private IntentSlotPOMapper mapper;

    @InjectMocks
    private IntentSlotService intentSlotService;

    @Test
    void create_shouldReturnResponse() {
        String id = UUID.randomUUID().toString();
        IntentSlotPO po = IntentSlotPO.builder().id(id).build();
        when(mapper.insert(any())).thenReturn(1);

        CreateIntentSlotRequest req = CreateIntentSlotRequest.builder().build();
        IntentSlotResponse resp = intentSlotService.create("onto-1", req, "test-user");

        assertNotNull(resp);
        verify(mapper).insert(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        String id = UUID.randomUUID().toString();
        IntentSlotPO po = IntentSlotPO.builder().id(id).build();
        when(mapper.selectById(id)).thenReturn(po);

        IntentSlotResponse resp = intentSlotService.getById(id);
        assertNotNull(resp);
        assertEquals(id, resp.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(mapper.selectById("nonexistent")).thenReturn(null);
        assertNull(intentSlotService.getById("nonexistent"));
    }

    @Test
    void list_shouldReturnAll() {
        when(mapper.selectList(null)).thenReturn(List.of(
                IntentSlotPO.builder().id("1").build(),
                IntentSlotPO.builder().id("2").build()
        ));
        List<IntentSlotResponse> list = intentSlotService.list();
        assertEquals(2, list.size());
    }

    @Test
    void delete_shouldCallMapperDelete() {
        intentSlotService.delete("some-id");
        verify(mapper).deleteById("some-id");
    }
}
