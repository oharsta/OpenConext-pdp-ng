package org.openconext.pdp;

import org.junit.jupiter.api.Test;
import org.openconext.pdp.engine.ConditionMatcher;
import org.openconext.pdp.model.PdpAttribute;
import org.openconext.pdp.model.RequestAttribute;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionMatcherTest {

    private final ConditionMatcher matcher = new ConditionMatcher();

    @Test
    void matchesExactValue() {
        PdpAttribute policyAttribute = new PdpAttribute("urn:test", "admin", 0, false);
        RequestAttribute requestAttribute = new RequestAttribute("urn:test", "admin");
        assertThat(matcher.matches(policyAttribute, List.of(requestAttribute))).isTrue();
    }

    @Test
    void matchesRegex() {
        PdpAttribute policyAttribute = new PdpAttribute("urn:test", "^(18|19|20)$", 0, false);
        RequestAttribute requestAttribute = new RequestAttribute("urn:test", "19");
        assertThat(matcher.matches(policyAttribute, List.of(requestAttribute))).isTrue();
    }

    @Test
    void handlesNegation() {
        PdpAttribute policyAttribute = new PdpAttribute("urn:test", "blocked", 0, false);
        policyAttribute = new PdpAttribute(policyAttribute.name(), policyAttribute.value(), policyAttribute.groupID(), true);
        RequestAttribute requestAttribute = new RequestAttribute("urn:test", "allowed");
        assertThat(matcher.matches(policyAttribute, List.of(requestAttribute))).isTrue();
    }
}
