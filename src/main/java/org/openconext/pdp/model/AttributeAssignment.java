package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AttributeAssignment(
        @JsonProperty("Category") String category,
        @JsonProperty("AttributeId") String attributeId,
        @JsonProperty("Value") Object value,
        @JsonProperty("DataType") String dataType
) {
}
