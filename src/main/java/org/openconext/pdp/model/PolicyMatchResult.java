package org.openconext.pdp.model;

import java.util.ArrayList;
import java.util.List;

public record PolicyMatchResult(
        Decision decision,
        boolean matched,
        boolean indeterminate,
        String policyId,
        String denyAdvice,
        String denyAdviceNl,
        boolean idpOnly,
        String loaLevel,
        String missingAttributeId,
        List<Category> categories
) {
    public PolicyMatchResult {
        decision = decision == null ? Decision.Deny : decision;
        categories = categories == null ? new ArrayList<>() : categories;
    }
}
