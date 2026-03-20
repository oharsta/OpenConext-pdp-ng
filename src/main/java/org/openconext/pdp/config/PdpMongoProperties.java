package org.openconext.pdp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pdp.mongo")
@Getter
@Setter
public class PdpMongoProperties {

    private String policiesCollection = "policies";
    private String policyVersionCollection = "policy_push_version";
    private String policyVersionId = "policy_push_version";

}
