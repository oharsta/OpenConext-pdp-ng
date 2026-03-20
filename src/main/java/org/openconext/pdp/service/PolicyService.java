package org.openconext.pdp.service;

import org.openconext.pdp.config.PdpMongoProperties;
import org.openconext.pdp.engine.PolicyCache;
import org.openconext.pdp.model.PdpPolicyDefinition;
import org.openconext.pdp.model.PolicyDocument;
import org.openconext.pdp.model.PolicyPushVersion;
import org.openconext.pdp.repository.PolicyCacheRepository;
import org.openconext.pdp.repository.PolicyDocumentRepository;
import org.openconext.pdp.repository.PolicyPushVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PolicyService {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyService.class);

    private final PolicyDocumentRepository policyDocumentRepository;
    private final PolicyPushVersionRepository policyPushVersionRepository;
    private final PolicyCacheRepository policyCacheRepository;
    private final PdpMongoProperties properties;

    public PolicyService(PolicyDocumentRepository policyDocumentRepository,
                         PolicyPushVersionRepository policyPushVersionRepository,
                         PolicyCacheRepository policyCacheRepository,
                         PdpMongoProperties properties) {
        this.policyDocumentRepository = policyDocumentRepository;
        this.policyPushVersionRepository = policyPushVersionRepository;
        this.policyCacheRepository = policyCacheRepository;
        this.properties = properties;
    }

    @Transactional
    public void replacePolicies(List<PdpPolicyDefinition> policies) {
        List<PolicyDocument> documents = policies.stream()
                .map(PolicyDocument::new)
                .collect(Collectors.toList());

        policyDocumentRepository.deleteAll();
        policyDocumentRepository.saveAll(documents);

        PolicyPushVersion existing = policyPushVersionRepository.findById(properties.getPolicyVersionId())
                .orElse(new PolicyPushVersion(properties.getPolicyVersionId(), 0));
        PolicyPushVersion updated = new PolicyPushVersion(existing.id(), existing.version() + 1);
        policyPushVersionRepository.save(updated);
        LOG.info("Policies replaced. New version {}", updated.version());
    }

    public void refreshIfNeeded() {
        PolicyCache cache = policyCacheRepository.getCache();
        long current = policyPushVersionRepository.findById(properties.getPolicyVersionId())
                .orElse(new PolicyPushVersion(properties.getPolicyVersionId(), 0)).version();
        if (current != cache.version()) {
            policyCacheRepository.reload();
        }
    }

    public void reload() {
        policyCacheRepository.reload();
    }
}
