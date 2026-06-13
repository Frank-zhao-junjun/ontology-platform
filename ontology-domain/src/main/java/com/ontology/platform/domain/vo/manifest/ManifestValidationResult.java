package com.ontology.platform.domain.vo.manifest;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManifestValidationResult {
    private boolean valid;
    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();
    @Builder.Default
    private List<ValidationError> warnings = new ArrayList<>();

    public void addError(ValidationError error) {
        if (error.isWarning()) {
            warnings.add(error);
        } else {
            errors.add(error);
            valid = false;
        }
    }

    public static ManifestValidationResult success() {
        return ManifestValidationResult.builder().valid(true).build();
    }
}
