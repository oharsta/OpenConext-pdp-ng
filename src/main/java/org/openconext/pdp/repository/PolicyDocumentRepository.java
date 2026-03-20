package org.openconext.pdp.repository;

import org.openconext.pdp.model.PolicyDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PolicyDocumentRepository extends MongoRepository<PolicyDocument, String> {
}
