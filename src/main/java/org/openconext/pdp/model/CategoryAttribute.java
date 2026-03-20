package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CategoryAttribute(
        @JsonProperty("AttributeId") String attributeId,
        @JsonProperty("Value") Object value,
        @JsonProperty("DataType") String dataType
) {
}
