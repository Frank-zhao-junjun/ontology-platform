package com.ontology.platform.infrastructure.validation;

import com.ontology.platform.domain.dto.imports.OntologyExchangeDocument;
import com.ontology.platform.domain.service.validation.ValidationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrganizationValidator Test")
class OrganizationValidatorTest {

    private final OrganizationValidator validator = new OrganizationValidator();

    @Test
    @DisplayName("should pass when organizationModel is null")
    void passWhenNoOrganization() {
        var doc = OntologyExchangeDocument.builder()
                .spec(OntologyExchangeDocument.Spec.builder()
                        .project(OntologyExchangeDocument.OntologyProject.builder().build())
                        .build())
                .build();
        var ctx = new ValidationContext(doc, "strict", "test");
        assertThat(validator.validate(ctx)).isEmpty();
    }

    @Test
    @DisplayName("should detect missing department id")
    void detectMissingDepartmentId() {
        var doc = OntologyExchangeDocument.builder()
                .spec(OntologyExchangeDocument.Spec.builder()
                        .project(OntologyExchangeDocument.OntologyProject.builder()
                                .organizationModel(OntologyExchangeDocument.OrganizationModel.builder()
                                        .departments(List.of(
                                                OntologyExchangeDocument.Department.builder()
                                                        .name("生产部")
                                                        .build()))
                                        .build())
                                .build())
                        .build())
                .build();
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "VM-O-01".equals(i.getCode()));
    }

    @Test
    @DisplayName("should detect position referencing unknown department")
    void detectInvalidPositionDepartment() {
        var doc = OntologyExchangeDocument.builder()
                .spec(OntologyExchangeDocument.Spec.builder()
                        .project(OntologyExchangeDocument.OntologyProject.builder()
                                .organizationModel(OntologyExchangeDocument.OrganizationModel.builder()
                                        .departments(List.of(
                                                OntologyExchangeDocument.Department.builder()
                                                        .id("dept-1").name("生产部").build()))
                                        .positions(List.of(
                                                OntologyExchangeDocument.Position.builder()
                                                        .id("pos-1").name("计划员")
                                                        .departmentId("dept-unknown").build()))
                                        .build())
                                .build())
                        .build())
                .build();
        var issues = validator.validate(new ValidationContext(doc, "strict", "test"));
        assertThat(issues).anyMatch(i -> "VM-O-05".equals(i.getCode()));
    }
}
