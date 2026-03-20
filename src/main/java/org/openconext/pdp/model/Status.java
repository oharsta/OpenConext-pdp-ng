package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"StatusDetail", "StatusCode", "StatusMessage"})
public record Status(
        @JsonProperty("StatusCode") StatusCode statusCode,
        @JsonProperty("StatusMessage") String statusMessage,
        @JsonProperty("StatusDetail") String statusDetail
) {
}
