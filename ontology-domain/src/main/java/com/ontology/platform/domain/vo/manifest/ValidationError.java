package com.ontology.platform.domain.vo.manifest;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    private String code;
    private String elementType;
    private String id;
    private String field;
    private String message;
    private String severity;

    public static ValidationError of(String code, String elementType, String id,
                                      String field, String message) {
        return ValidationError.builder()
                .code(code).elementType(elementType).id(id)
                .field(field).message(message).severity("ERROR").build();
    }

    public static ValidationError warning(String code, String elementType, String id,
                                           String field, String message) {
        return ValidationError.builder()
                .code(code).elementType(elementType).id(id)
                .field(field).message(message).severity("WARNING").build();
    }

    public boolean isWarning() { return "WARNING".equals(severity); }
}
