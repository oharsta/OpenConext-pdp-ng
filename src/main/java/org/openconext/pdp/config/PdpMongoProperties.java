package org.openconext.pdp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pdp.mongo")
public class PdpMongoProperties {

    private String policiesCollection = "policies";
    private String policyVersionCollection = "policy_push_version";
    private String policyVersionId = "policy_push_version";

    public String getPoliciesCollection() {
        return policiesCollection;
    }

    public void setPoliciesCollection(String policiesCollection) {
        this.policiesCollection = policiesCollection;
    }

    public String getPolicyVersionCollection() {
        return policyVersionCollection;
    }

    public void setPolicyVersionCollection(String policyVersionCollection) {
        this.policyVersionCollection = policyVersionCollection;
    }

    public String getPolicyVersionId() {
        return policyVersionId;
    }

    public void setPolicyVersionId(String policyVersionId) {
        this.policyVersionId = policyVersionId;
    }
}
