package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PdpAttribute(
        @NotNull @Size(min = 1) String name,
        @NotNull @Size(min = 1) String value,
        int groupID,
        boolean negated
) {
    public PdpAttribute {
        value = value != null ? value.trim() : null;
    }
}
