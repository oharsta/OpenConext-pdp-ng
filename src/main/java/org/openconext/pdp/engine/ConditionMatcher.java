package org.openconext.pdp.engine;

import org.openconext.pdp.model.PdpAttribute;
import org.openconext.pdp.model.RequestAttribute;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class ConditionMatcher {

    public String findMatchedValue(PdpAttribute policyAttribute, List<RequestAttribute> requestValues) {
        if (requestValues == null || requestValues.isEmpty()) {
            return null;
        }
        String policyValue = policyAttribute.value();
        boolean negated = policyAttribute.negated();

        if (negated) {
            boolean anyMatch = requestValues.stream()
                    .map(RequestAttribute::value)
                    .filter(Objects::nonNull)
                    .anyMatch(value -> matchesRegex(policyValue, value));
            if (anyMatch) {
                return null;
            }
            for (RequestAttribute requestAttribute : requestValues) {
                if (requestAttribute.value() != null) {
                    return requestAttribute.value();
                }
            }
            return null;
        }

        for (RequestAttribute requestAttribute : requestValues) {
            String requestValue = requestAttribute.value();
            if (requestValue == null) {
                continue;
            }
            if (matchesRegex(policyValue, requestValue)) {
                return requestValue;
            }
        }
        return null;
    }

    public boolean matches(PdpAttribute policyAttribute, List<RequestAttribute> requestValues) {
        return findMatchedValue(policyAttribute, requestValues) != null;
    }

    private boolean matchesRegex(String pattern, String value) {
        return pattern != null && Pattern.compile(pattern).matcher(value).matches();
    }
}
