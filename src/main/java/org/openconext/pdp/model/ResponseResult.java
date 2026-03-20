package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResponseResult(
        @JsonProperty("Status") Status status,
        @JsonProperty("Obligations") @JsonInclude(JsonInclude.Include.NON_EMPTY) List<Obligation> obligations,
        @JsonProperty("AssociatedAdvice") @JsonInclude(JsonInclude.Include.NON_EMPTY) List<AssociatedAdvice> associatedAdvice,
        @JsonProperty("Category") @JsonInclude(JsonInclude.Include.NON_EMPTY) List<Category> category,
        @JsonProperty("PolicyIdentifier") PolicyIdentifier policyIdentifier,
        @JsonProperty("Decision") String decision
) {
    public ResponseResult {
        obligations = obligations == null ? new ArrayList<>() : obligations;
        associatedAdvice = associatedAdvice == null ? new ArrayList<>() : associatedAdvice;
        category = category == null ? new ArrayList<>() : category;
    }
}
