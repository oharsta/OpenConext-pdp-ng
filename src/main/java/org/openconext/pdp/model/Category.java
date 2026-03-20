package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Category(
        @JsonProperty("CategoryId") String categoryId,
        @JsonProperty("Attribute") List<CategoryAttribute> attribute
) {
    public Category {
        attribute = attribute == null ? new ArrayList<>() : attribute;
    }
}
