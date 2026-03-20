package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributeContainer(
        @JsonProperty("Attribute") List<RequestAttribute> attribute
) {
    public AttributeContainer {
        attribute = attribute == null ? new ArrayList<>() : attribute;
    }
}
