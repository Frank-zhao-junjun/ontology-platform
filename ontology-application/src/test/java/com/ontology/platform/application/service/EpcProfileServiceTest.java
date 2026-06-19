package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateEpcProfileRequest;
import com.ontology.platform.application.dto.domain.EpcProfileResponse;
import com.ontology.platform.infrastructure.persistence.EpcProfilePO;
import com.ontology.platform.infrastructure.persistence.EpcProfilePOMapper;
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
class EpcProfileServiceTest {

    @Mock
    private EpcProfilePOMapper mapper;

    @InjectMocks
    private EpcProfileService epcProfileService;

    @Test
    void create_shouldReturnResponse() {
        String id = UUID.randomUUID().toString();
        EpcProfilePO po = EpcProfilePO.builder().id(id).build();
        when(mapper.insert(any())).thenReturn(1);

        CreateEpcProfileRequest req = CreateEpcProfileRequest.builder().build();
        EpcProfileResponse resp = epcProfileService.create("onto-1", req, "test-user");

        assertNotNull(resp);
        verify(mapper).insert(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        String id = UUID.randomUUID().toString();
        EpcProfilePO po = EpcProfilePO.builder().id(id).build();
        when(mapper.selectById(id)).thenReturn(po);

        EpcProfileResponse resp = epcProfileService.getById(id);
        assertNotNull(resp);
        assertEquals(id, resp.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(mapper.selectById("nonexistent")).thenReturn(null);
        assertNull(epcProfileService.getById("nonexistent"));
    }

    @Test
    void list_shouldReturnAll() {
        when(mapper.selectList(null)).thenReturn(List.of(
                EpcProfilePO.builder().id("1").build(),
                EpcProfilePO.builder().id("2").build()
        ));
        List<EpcProfileResponse> list = epcProfileService.list();
        assertEquals(2, list.size());
    }

    @Test
    void delete_shouldCallMapperDelete() {
        epcProfileService.delete("some-id");
        verify(mapper).deleteById("some-id");
    }
}
