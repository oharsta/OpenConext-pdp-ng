package org.openconext.pdp.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "policy_push_version")
public record PolicyPushVersion(
        @Id String id,
        long version
) {
}
