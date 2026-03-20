package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PolicyIdentifier(
        @JsonProperty("PolicySetIdReference") List<PolicyIdReference> policySetIdReference,
        @JsonProperty("PolicyIdReference") @JsonInclude(JsonInclude.Include.NON_EMPTY) List<PolicyIdReference> policyIdReference
) {
    public PolicyIdentifier {
        policySetIdReference = policySetIdReference == null ? new ArrayList<>() : policySetIdReference;
        policyIdReference = policyIdReference == null ? new ArrayList<>() : policyIdReference;
    }
}
