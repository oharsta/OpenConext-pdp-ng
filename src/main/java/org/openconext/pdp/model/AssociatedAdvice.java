package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record AssociatedAdvice(
        @JsonProperty("AttributeAssignment") List<AttributeAssignment> attributeAssignment,
        @JsonProperty("Id") String id
) {
    public AssociatedAdvice {
        attributeAssignment = attributeAssignment == null ? new ArrayList<>() : attributeAssignment;
    }
}
