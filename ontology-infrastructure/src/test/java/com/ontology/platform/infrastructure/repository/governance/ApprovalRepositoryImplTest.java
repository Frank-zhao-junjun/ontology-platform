package com.ontology.platform.infrastructure.repository.governance;

import com.ontology.platform.domain.entity.governance.ApprovalRequest;
import com.ontology.platform.infrastructure.converter.ApprovalRequestConverter;
import com.ontology.platform.infrastructure.persistence.ApprovalRequestPO;
import com.ontology.platform.infrastructure.persistence.ApprovalRequestPOMapper;
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

@DisplayName("ApprovalRepositoryImpl 测试")
class ApprovalRepositoryImplTest {

    private ApprovalRepositoryImpl repository;
    private ApprovalRequestPOMapper mapper;
    private ApprovalRequestConverter converter;

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(ApprovalRequestPOMapper.class);
        converter = new ApprovalRequestConverter();
        repository = new ApprovalRepositoryImpl(mapper, converter);
    }

    private ApprovalRequestPO buildPO(String id, String agentId, String status) {
        return ApprovalRequestPO.builder()
                .id(id)
                .agentId(agentId)
                .actionId("action-1")
                .requestedOp("WRITE")
                .status(status)
                .requestedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("save - 记录不存在时执行 insert")
    void save_insertWhenNotExists() {
        ApprovalRequest request = ApprovalRequest.submit("agent-1", "action-1", "WRITE");
        Mockito.when(mapper.selectById(request.getId())).thenReturn(null);

        ApprovalRequest saved = repository.save(request);

        assertThat(saved.getId()).isEqualTo(request.getId());
        Mockito.verify(mapper).selectById(request.getId());
        Mockito.verify(mapper).insert(any(ApprovalRequestPO.class));
        Mockito.verify(mapper, Mockito.never()).updateById(any());
    }

    @Test
    @DisplayName("save - 记录已存在时执行 updateById")
    void save_updateWhenExists() {
        ApprovalRequest request = ApprovalRequest.submit("agent-1", "action-1", "WRITE");
        ApprovalRequestPO existing = buildPO(request.getId(), "agent-1", "PENDING");
        Mockito.when(mapper.selectById(request.getId())).thenReturn(existing);

        ApprovalRequest saved = repository.save(request);

        assertThat(saved.getId()).isEqualTo(request.getId());
        Mockito.verify(mapper).selectById(request.getId());
        Mockito.verify(mapper).updateById(any(ApprovalRequestPO.class));
        Mockito.verify(mapper, Mockito.never()).insert(any());
    }

    @Test
    @DisplayName("save - 审批通过后能正确更新状态")
    void save_updatesResolvedStatus() {
        ApprovalRequest request = ApprovalRequest.submit("agent-1", "action-1", "WRITE");
        request.approve("admin");
        ApprovalRequestPO existing = buildPO(request.getId(), "agent-1", "PENDING");
        Mockito.when(mapper.selectById(request.getId())).thenReturn(existing);

        repository.save(request);

        Mockito.verify(mapper).updateById(any(ApprovalRequestPO.class));
    }

    @Test
    @DisplayName("findById - 命中时返回 Entity")
    void findById_hit() {
        String id = UUID.randomUUID().toString();
        ApprovalRequestPO po = buildPO(id, "agent-1", "PENDING");
        Mockito.when(mapper.selectById(id)).thenReturn(po);

        Optional<ApprovalRequest> result = repository.findById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        assertThat(result.get().getAgentId()).isEqualTo("agent-1");
        assertThat(result.get().getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("findById - 未命中时返回空 Optional")
    void findById_miss() {
        Mockito.when(mapper.selectById(any())).thenReturn(null);

        assertThat(repository.findById("nope")).isEmpty();
    }

    @Test
    @DisplayName("findByAgentId - 返回 Entity 列表")
    void findByAgentId() {
        String agentId = "agent-1";
        List<ApprovalRequestPO> poList = List.of(
                buildPO(UUID.randomUUID().toString(), agentId, "PENDING"),
                buildPO(UUID.randomUUID().toString(), agentId, "APPROVED"));
        Mockito.when(mapper.selectByAgentId(agentId)).thenReturn(poList);

        List<ApprovalRequest> result = repository.findByAgentId(agentId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ApprovalRequest::getStatus).containsExactly("PENDING", "APPROVED");
        Mockito.verify(mapper).selectByAgentId(eq(agentId));
    }

    @Test
    @DisplayName("findByAgentId - 空结果")
    void findByAgentId_empty() {
        Mockito.when(mapper.selectByAgentId("no-agent")).thenReturn(Collections.emptyList());

        List<ApprovalRequest> result = repository.findByAgentId("no-agent");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findPending - 返回待审批列表")
    void findPending() {
        List<ApprovalRequestPO> poList = List.of(
                buildPO(UUID.randomUUID().toString(), "agent-1", "PENDING"),
                buildPO(UUID.randomUUID().toString(), "agent-2", "PENDING"));
        Mockito.when(mapper.selectPending()).thenReturn(poList);

        List<ApprovalRequest> result = repository.findPending();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(r -> "PENDING".equals(r.getStatus()));
        Mockito.verify(mapper).selectPending();
    }

    @Test
    @DisplayName("findPending - 无待审批项时返回空列表")
    void findPending_empty() {
        Mockito.when(mapper.selectPending()).thenReturn(Collections.emptyList());

        List<ApprovalRequest> result = repository.findPending();

        assertThat(result).isEmpty();
        Mockito.verify(mapper).selectPending();
    }
}
