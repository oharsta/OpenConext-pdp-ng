package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthorizationResponse(
        @JsonProperty("Response") @JsonInclude(JsonInclude.Include.NON_EMPTY) List<ResponseResult> response
) {
    public AuthorizationResponse {
        response = response == null ? new ArrayList<>() : response;
    }
}
