package org.openconext.pdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.openconext.pdp.model.PdpPolicyDefinition;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyParsingTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesPolicyDefinition() throws Exception {
        Path path = Path.of("src/test/resources/test-harness/reg_permit_basic_match/policy.json");
        String json = Files.readString(path);
        PdpPolicyDefinition policy = objectMapper.readValue(json, PdpPolicyDefinition.class);
        assertThat(policy.name()).isEqualTo("permit_basic_match");
        assertThat(policy.serviceProviderIds()).contains("https://sp.example.org/shibboleth");
    }
}
