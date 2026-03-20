package org.openconext.pdp.repository;

import org.openconext.pdp.config.PdpMongoProperties;
import org.openconext.pdp.engine.PolicyCache;
import org.openconext.pdp.model.PdpPolicyDefinition;
import org.openconext.pdp.model.PolicyDocument;
import org.openconext.pdp.model.PolicyPushVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Repository
public class MongoPolicyCacheRepository implements PolicyCacheRepository {

    private static final Logger LOG = LoggerFactory.getLogger(MongoPolicyCacheRepository.class);

    private final PolicyDocumentRepository policyDocumentRepository;
    private final PolicyPushVersionRepository policyPushVersionRepository;
    private final PdpMongoProperties properties;
    private final AtomicReference<PolicyCache> cache;

    public MongoPolicyCacheRepository(PolicyDocumentRepository policyDocumentRepository,
                                      PolicyPushVersionRepository policyPushVersionRepository,
                                      PdpMongoProperties properties) {
        this.policyDocumentRepository = policyDocumentRepository;
        this.policyPushVersionRepository = policyPushVersionRepository;
        this.properties = properties;
        this.cache = new AtomicReference<>(new PolicyCache(List.of(), 0));
        reload();
    }

    @Override
    public PolicyCache getCache() {
        return cache.get();
    }

    @Override
    public void reload() {
        List<PolicyDocument> documents = policyDocumentRepository.findAll();
        List<PdpPolicyDefinition> policies = documents.stream()
                .map(PolicyDocument::policy)
                .collect(Collectors.toList());
        PolicyPushVersion version = policyPushVersionRepository.findById(properties.getPolicyVersionId())
                .orElse(new PolicyPushVersion(properties.getPolicyVersionId(), 0));
        cache.set(new PolicyCache(policies, version.version()));
        LOG.info("Loaded {} policies at version {}", policies.size(), version.version());
    }
}
