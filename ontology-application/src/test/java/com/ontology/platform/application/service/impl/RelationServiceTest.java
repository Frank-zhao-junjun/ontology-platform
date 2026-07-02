package com.ontology.platform.application.service.impl;

import com.ontology.platform.application.dto.CreateRelationRequest;
import com.ontology.platform.common.enums.RelationCardinality;
import com.ontology.platform.common.exception.ValidationException;
import com.ontology.platform.domain.entity.ObjectType;
import com.ontology.platform.domain.entity.Ontology;
import com.ontology.platform.domain.repository.ObjectTypeRepository;
import com.ontology.platform.domain.repository.OntologyRepository;
import com.ontology.platform.domain.repository.RelationRepository;
import com.ontology.platform.infrastructure.graph.GraphService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * RelationServiceImpl 源/目标本体一致性校验测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RelationServiceImpl 本体一致性校验测试")
class RelationServiceTest {

    @Mock
    private RelationRepository relationRepository;

    @Mock
    private ObjectTypeRepository objectTypeRepository;

    @Mock
    private OntologyRepository ontologyRepository;

    @Mock
    private GraphService graphService;

    @InjectMocks
    private RelationServiceImpl relationService;

    @Test
    @DisplayName("目标对象类型不属于指定本体时应抛出异常")
    void shouldThrowExceptionWhenTargetTypeNotBelongToOntology() {
        String ontologyId = "ontology-1";
        String differentOntologyId = "ontology-2";
        String sourceId = "source-type-id";
        String targetId = "target-type-id";

        Ontology ontology = Ontology.create("test-ontology", "测试本体", "描述", "user-001");
        ontology.setId(ontologyId);

        ObjectType sourceType = ObjectType.create(
                ontologyId, "EMPLOYEE", "员工", "员工对象类型", "emp_id"
        );
        sourceType.setId(sourceId);

        ObjectType targetType = ObjectType.create(
                differentOntologyId, "DEPARTMENT", "部门", "部门对象类型", "dept_id"
        );
        targetType.setId(targetId);

        CreateRelationRequest request = CreateRelationRequest.builder()
                .ontologyId(ontologyId)
                .sourceTypeId(sourceId)
                .targetTypeId(targetId)
                .name("WORKS_IN")
                .displayName("所属部门")
                .cardinality(RelationCardinality.MANY_TO_ONE)
                .build();

        when(ontologyRepository.findById(ontologyId)).thenReturn(Optional.of(ontology));
        when(objectTypeRepository.findById(sourceId)).thenReturn(Optional.of(sourceType));
        when(objectTypeRepository.findById(targetId)).thenReturn(Optional.of(targetType));

        assertThrows(ValidationException.class, () -> relationService.createRelation(request));

        verify(relationRepository, never()).save(any());
        verify(graphService, never()).createEdge(any(), any(), any());
    }
}
