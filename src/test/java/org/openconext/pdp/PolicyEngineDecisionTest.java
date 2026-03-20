package org.openconext.pdp;

import org.junit.jupiter.api.Test;
import org.openconext.pdp.engine.PolicyEngineImpl;
import org.openconext.pdp.model.AuthorizationRequest;
import org.openconext.pdp.model.AuthorizationResponse;
import org.openconext.pdp.model.PdpPolicyDefinition;
import org.openconext.pdp.repository.PolicyCacheRepository;
import org.openconext.pdp.engine.PolicyCache;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyEngineDecisionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void returnsDenyWhenNoMatch() throws Exception {
        PdpPolicyDefinition policy = readPolicy("reg_deny_attribute_value_mismatch");
        AuthorizationRequest request = readRequest("reg_deny_attribute_value_mismatch");
        PolicyCacheRepository cacheRepository = new InMemoryPolicyCache(List.of(policy));
        PolicyEngineImpl engine = new PolicyEngineImpl(cacheRepository);
        AuthorizationResponse response = engine.decide(request);
        assertThat(response.response().get(0).decision()).isEqualTo("Deny");
    }

    private PdpPolicyDefinition readPolicy(String dir) throws Exception {
        Path path = Path.of("src/test/resources/test-harness/" + dir + "/policy.json");
        return objectMapper.readValue(Files.readString(path), PdpPolicyDefinition.class);
    }

    private AuthorizationRequest readRequest(String dir) throws Exception {
        Path path = Path.of("src/test/resources/test-harness/" + dir + "/request.json");
        return objectMapper.readValue(Files.readString(path), AuthorizationRequest.class);
    }

    static class InMemoryPolicyCache implements PolicyCacheRepository {
        private final PolicyCache cache;

        InMemoryPolicyCache(List<PdpPolicyDefinition> policies) {
            this.cache = new PolicyCache(policies, 0);
        }

        @Override
        public PolicyCache getCache() {
            return cache;
        }

        @Override
        public void reload() {
        }
    }
}
