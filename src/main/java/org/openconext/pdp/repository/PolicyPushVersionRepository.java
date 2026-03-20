package org.openconext.pdp.repository;

import org.openconext.pdp.model.PolicyPushVersion;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PolicyPushVersionRepository extends MongoRepository<PolicyPushVersion, String> {
}
