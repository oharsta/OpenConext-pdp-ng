package org.openconext.pdp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openconext.pdp.engine.PolicyEngine;
import org.openconext.pdp.model.AuthorizationRequest;
import org.openconext.pdp.model.AuthorizationResponse;
import org.openconext.pdp.model.PdpPolicyDefinition;
import org.openconext.pdp.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PolicyEngineIntegrationTest {

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", () -> "mongodb://localhost:27017/pdp_ng_test");
    }

    @Autowired
    private PolicyService policyService;

    @Autowired
    private PolicyEngine policyEngine;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws IOException {
        PdpPolicyDefinition policy = readPolicy("reg_permit_basic_match");
        policyService.replacePolicies(List.of(policy));
        policyService.reload();
    }

    @Test
    void evaluatesPermitPolicy() throws IOException {
        AuthorizationRequest request = readRequest("reg_permit_basic_match");
        AuthorizationResponse response = policyEngine.decide(request);
        String decision = response.response().get(0).decision();
        assertThat(decision).isEqualTo("Permit");
    }

    private PdpPolicyDefinition readPolicy(String name) throws IOException {
        Path path = Path.of("src/test/resources/test-harness/" + name + "/policy.json");
        return objectMapper.readValue(Files.readString(path), PdpPolicyDefinition.class);
    }

    private AuthorizationRequest readRequest(String name) throws IOException {
        Path path = Path.of("src/test/resources/test-harness/" + name + "/request.json");
        return objectMapper.readValue(Files.readString(path), AuthorizationRequest.class);
    }
}
