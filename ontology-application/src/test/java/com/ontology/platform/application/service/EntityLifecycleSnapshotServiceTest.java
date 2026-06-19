package com.ontology.platform.application.service;

import com.ontology.platform.application.dto.domain.CreateEntityLifecycleSnapshotRequest;
import com.ontology.platform.application.dto.domain.EntityLifecycleSnapshotResponse;
import com.ontology.platform.infrastructure.persistence.EntityLifecycleSnapshotPO;
import com.ontology.platform.infrastructure.persistence.EntityLifecycleSnapshotPOMapper;
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
class EntityLifecycleSnapshotServiceTest {

    @Mock
    private EntityLifecycleSnapshotPOMapper mapper;

    @InjectMocks
    private EntityLifecycleSnapshotService entityLifecycleSnapshotService;

    @Test
    void create_shouldReturnResponse() {
        String id = UUID.randomUUID().toString();
        EntityLifecycleSnapshotPO po = EntityLifecycleSnapshotPO.builder().id(id).build();
        when(mapper.insert(any())).thenReturn(1);

        CreateEntityLifecycleSnapshotRequest req = CreateEntityLifecycleSnapshotRequest.builder().build();
        EntityLifecycleSnapshotResponse resp = entityLifecycleSnapshotService.create("onto-1", req, "test-user");

        assertNotNull(resp);
        verify(mapper).insert(any());
    }

    @Test
    void getById_shouldReturnResponse_whenFound() {
        String id = UUID.randomUUID().toString();
        EntityLifecycleSnapshotPO po = EntityLifecycleSnapshotPO.builder().id(id).build();
        when(mapper.selectById(id)).thenReturn(po);

        EntityLifecycleSnapshotResponse resp = entityLifecycleSnapshotService.getById(id);
        assertNotNull(resp);
        assertEquals(id, resp.getId());
    }

    @Test
    void getById_shouldReturnNull_whenNotFound() {
        when(mapper.selectById("nonexistent")).thenReturn(null);
        assertNull(entityLifecycleSnapshotService.getById("nonexistent"));
    }

    @Test
    void list_shouldReturnAll() {
        when(mapper.selectList(null)).thenReturn(List.of(
                EntityLifecycleSnapshotPO.builder().id("1").build(),
                EntityLifecycleSnapshotPO.builder().id("2").build()
        ));
        List<EntityLifecycleSnapshotResponse> list = entityLifecycleSnapshotService.list();
        assertEquals(2, list.size());
    }

    @Test
    void delete_shouldCallMapperDelete() {
        entityLifecycleSnapshotService.delete("some-id");
        verify(mapper).deleteById("some-id");
    }
}
