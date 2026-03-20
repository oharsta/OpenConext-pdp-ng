package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PolicyIdReference(
        @JsonProperty("Version") String version,
        @JsonProperty("Id") String id
) {
}
