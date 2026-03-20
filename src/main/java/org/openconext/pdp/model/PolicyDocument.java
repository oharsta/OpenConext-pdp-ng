package org.openconext.pdp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "policies")
public record PolicyDocument(
        @Id String id,
        PdpPolicyDefinition policy
) {
    public PolicyDocument(PdpPolicyDefinition policy) {
        this(policy != null ? policy.id() : null, policy);
    }
}
