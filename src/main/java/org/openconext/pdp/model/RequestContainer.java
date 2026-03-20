package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RequestContainer(
        @JsonProperty("AccessSubject") AttributeContainer accessSubject,
        @JsonProperty("Resource") AttributeContainer resource
) {
}
