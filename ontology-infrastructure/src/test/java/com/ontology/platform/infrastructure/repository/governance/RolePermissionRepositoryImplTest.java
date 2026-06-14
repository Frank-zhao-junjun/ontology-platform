package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.RolePermission;
import com.ontology.platform.infrastructure.converter.RolePermissionConverter;
import com.ontology.platform.infrastructure.persistence.RolePermissionPO;
import com.ontology.platform.infrastructure.persistence.RolePermissionPOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@DisplayName("RolePermissionRepositoryImpl 测试")
class RolePermissionRepositoryImplTest {

    private RolePermissionRepositoryImpl repository;
    private RolePermissionPOMapper mapper;
    private RolePermissionConverter converter;

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(RolePermissionPOMapper.class);
        converter = new RolePermissionConverter();
        repository = new RolePermissionRepositoryImpl(mapper, converter);
    }

    private RolePermissionPO buildPO(String id, String roleId, String domain) {
        RolePermissionPO po = RolePermissionPO.builder()
                .id(id)
                .roleId(roleId)
                .resource("ontology")
                .domain(domain)
                .createdAt(Instant.now())
                .build();
        po.setOperationsList(List.of("READ", "WRITE"));
        return po;
    }

    @Test
    @DisplayName("save - 写入 mapper 并返回 Entity")
    void save_inserts() {
        RolePermission perm = RolePermission.create(
                "role-1", "ontology", List.of("READ", "WRITE"), "domain-1");

        RolePermission saved = repository.save(perm);

        assertThat(saved.getId()).isEqualTo(perm.getId());
        assertThat(saved.getRoleId()).isEqualTo("role-1");
        Mockito.verify(mapper).insert(any(RolePermissionPO.class));
    }

    @Test
    @DisplayName("save - 保留操作列表和创建时间")
    void save_preservesOperationsAndCreatedAt() {
        RolePermission perm = RolePermission.create(
                "role-1", "ontology", List.of("READ", "WRITE", "DELETE"), "domain-1");

        RolePermission saved = repository.save(perm);

        assertThat(saved.getOperations()).containsExactly("READ", "WRITE", "DELETE");
        assertThat(saved.getCreatedAt()).isNotNull();
        Mockito.verify(mapper).insert(any(RolePermissionPO.class));
    }

    @Test
    @DisplayName("findByRoleId - 返回 Entity 列表")
    void findByRoleId() {
        String roleId = "role-1";
        List<RolePermissionPO> poList = List.of(
                buildPO(UUID.randomUUID().toString(), roleId, "domain-1"),
                buildPO(UUID.randomUUID().toString(), roleId, "domain-2"));
        Mockito.when(mapper.selectByRoleId(roleId)).thenReturn(poList);

        List<RolePermission> result = repository.findByRoleId(roleId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RolePermission::getDomain).containsExactly("domain-1", "domain-2");
        Mockito.verify(mapper).selectByRoleId(eq(roleId));
    }

    @Test
    @DisplayName("findByRoleId - 空结果")
    void findByRoleId_empty() {
        Mockito.when(mapper.selectByRoleId("no-role")).thenReturn(Collections.emptyList());

        List<RolePermission> result = repository.findByRoleId("no-role");

        assertThat(result).isEmpty();
        Mockito.verify(mapper).selectByRoleId(eq("no-role"));
    }

    @Test
    @DisplayName("findByDomain - 返回 Entity 列表")
    void findByDomain() {
        String domain = "domain-1";
        List<RolePermissionPO> poList = List.of(
                buildPO(UUID.randomUUID().toString(), "role-1", domain),
                buildPO(UUID.randomUUID().toString(), "role-2", domain));
        Mockito.when(mapper.selectByDomain(domain)).thenReturn(poList);

        List<RolePermission> result = repository.findByDomain(domain);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RolePermission::getRoleId).containsExactly("role-1", "role-2");
        Mockito.verify(mapper).selectByDomain(eq(domain));
    }

    @Test
    @DisplayName("findByDomain - 空结果")
    void findByDomain_empty() {
        Mockito.when(mapper.selectByDomain("no-domain")).thenReturn(Collections.emptyList());

        List<RolePermission> result = repository.findByDomain("no-domain");

        assertThat(result).isEmpty();
        Mockito.verify(mapper).selectByDomain(eq("no-domain"));
    }

    @Test
    @DisplayName("findByRoleId - 返回的权限包含正确的 operations")
    void findByRoleId_containsOperations() {
        String roleId = "role-1";
        RolePermissionPO po = buildPO(UUID.randomUUID().toString(), roleId, "domain-1");
        Mockito.when(mapper.selectByRoleId(roleId)).thenReturn(List.of(po));

        List<RolePermission> result = repository.findByRoleId(roleId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOperations()).containsExactly("READ", "WRITE");
    }
}
