package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.AgentRole;
import com.ontology.platform.infrastructure.converter.AgentRoleConverter;
import com.ontology.platform.infrastructure.persistence.AgentRolePO;
import com.ontology.platform.infrastructure.persistence.AgentRolePOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@DisplayName("AgentRoleRepositoryImpl 测试")
class AgentRoleRepositoryImplTest {

    private AgentRoleRepositoryImpl repository;
    private AgentRolePOMapper mapper;
    private AgentRoleConverter converter;

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(AgentRolePOMapper.class);
        converter = new AgentRoleConverter();
        repository = new AgentRoleRepositoryImpl(mapper, converter);
    }

    private AgentRolePO buildPO(String id, String tokenId, String domain, String role) {
        return AgentRolePO.builder()
                .id(id)
                .tokenId(tokenId)
                .domain(domain)
                .role(role)
                .grantedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("save - 写入 mapper 并返回 Entity")
    void save_inserts() {
        AgentRole role = AgentRole.create("token-1", "domain-1", "ADMIN");

        AgentRole saved = repository.save(role);

        assertThat(saved.getId()).isEqualTo(role.getId());
        assertThat(saved.getRole()).isEqualTo("ADMIN");
        Mockito.verify(mapper).insert(any(AgentRolePO.class));
    }

    @Test
    @DisplayName("save - 保留授予时间")
    void save_preservesGrantedAt() {
        AgentRole role = AgentRole.create("token-1", "domain-1", "EDITOR");

        repository.save(role);

        assertThat(role.getGrantedAt()).isNotNull();
        Mockito.verify(mapper).insert(any(AgentRolePO.class));
    }

    @Test
    @DisplayName("findById - 命中时返回 Entity")
    void findById_hit() {
        String id = UUID.randomUUID().toString();
        AgentRolePO po = buildPO(id, "token-1", "domain-1", "ADMIN");
        Mockito.when(mapper.selectById(id)).thenReturn(po);

        Optional<AgentRole> result = repository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getTokenId()).isEqualTo("token-1");
        assertThat(result.get().getDomain()).isEqualTo("domain-1");
        assertThat(result.get().getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("findById - 未命中时返回空 Optional")
    void findById_miss() {
        Mockito.when(mapper.selectById(any())).thenReturn(null);

        assertThat(repository.findById("nope")).isEmpty();
    }

    @Test
    @DisplayName("findByTokenId - 返回 Entity 列表")
    void findByTokenId() {
        String tokenId = "token-1";
        List<AgentRolePO> poList = List.of(
                buildPO(UUID.randomUUID().toString(), tokenId, "domain-1", "ADMIN"),
                buildPO(UUID.randomUUID().toString(), tokenId, "domain-2", "VIEWER"));
        Mockito.when(mapper.selectByTokenId(tokenId)).thenReturn(poList);

        List<AgentRole> result = repository.findByTokenId(tokenId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AgentRole::getRole).containsExactly("ADMIN", "VIEWER");
        Mockito.verify(mapper).selectByTokenId(eq(tokenId));
    }

    @Test
    @DisplayName("findByTokenId - 空结果")
    void findByTokenId_empty() {
        Mockito.when(mapper.selectByTokenId("token-missing")).thenReturn(Collections.emptyList());

        List<AgentRole> result = repository.findByTokenId("token-missing");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByDomain - 返回 Entity 列表")
    void findByDomain() {
        String domain = "domain-1";
        List<AgentRolePO> poList = List.of(
                buildPO(UUID.randomUUID().toString(), "token-1", domain, "ADMIN"),
                buildPO(UUID.randomUUID().toString(), "token-2", domain, "EDITOR"));
        Mockito.when(mapper.selectByDomain(domain)).thenReturn(poList);

        List<AgentRole> result = repository.findByDomain(domain);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(AgentRole::getTokenId).containsExactly("token-1", "token-2");
        Mockito.verify(mapper).selectByDomain(eq(domain));
    }

    @Test
    @DisplayName("findByDomain - 空结果")
    void findByDomain_empty() {
        Mockito.when(mapper.selectByDomain("nonexistent")).thenReturn(Collections.emptyList());

        List<AgentRole> result = repository.findByDomain("nonexistent");

        assertThat(result).isEmpty();
    }
}
