package org.openconext.pdp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.openconext.pdp.engine.PolicyEvaluator;
import org.openconext.pdp.model.AuthorizationRequest;
import org.openconext.pdp.model.PdpPolicyDefinition;
import org.openconext.pdp.model.PolicyMatchResult;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyEvaluatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void evaluatesPermitPolicy() throws Exception {
        PdpPolicyDefinition policy = readPolicy("reg_permit_basic_match");
        AuthorizationRequest request = readRequest("reg_permit_basic_match");
        PolicyEvaluator evaluator = new PolicyEvaluator();
        PolicyMatchResult result = evaluator.evaluate(policy, request);
        assertThat(result.matched()).isTrue();
        assertThat(result.decision().name()).isEqualTo("Permit");
    }

    @Test
    void evaluatesDenyPolicy() throws Exception {
        PdpPolicyDefinition policy = readPolicy("reg_deny_basic_match");
        AuthorizationRequest request = readRequest("reg_deny_basic_match");
        PolicyEvaluator evaluator = new PolicyEvaluator();
        PolicyMatchResult result = evaluator.evaluate(policy, request);
        assertThat(result.matched()).isTrue();
        assertThat(result.decision().name()).isEqualTo("Deny");
    }

    private PdpPolicyDefinition readPolicy(String dir) throws Exception {
        Path path = Path.of("src/test/resources/test-harness/" + dir + "/policy.json");
        return objectMapper.readValue(Files.readString(path), PdpPolicyDefinition.class);
    }

    private AuthorizationRequest readRequest(String dir) throws Exception {
        Path path = Path.of("src/test/resources/test-harness/" + dir + "/request.json");
        return objectMapper.readValue(Files.readString(path), AuthorizationRequest.class);
    }
}
