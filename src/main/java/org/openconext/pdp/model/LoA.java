package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LoA(
        String level,
        boolean allAttributesMustMatch,
        boolean negateCidrNotation,
        @Valid List<PdpAttribute> attributes,
        @Valid List<CidrNotation> cidrNotations
) {
    public LoA {
        attributes = attributes == null ? new ArrayList<>() : attributes;
        cidrNotations = cidrNotations == null ? new ArrayList<>() : cidrNotations;
    }

    @JsonIgnore
    public List<String> anyCidrNotations() {
        return CollectionUtils.isEmpty(this.cidrNotations) ? Collections.emptyList() : Arrays.asList("will-iterate-once");
    }

    @JsonIgnore
    public List<String> anyAttributes() {
        return CollectionUtils.isEmpty(this.attributes) ? Collections.emptyList() : Arrays.asList("will-iterate-once");
    }

    @JsonIgnore
    public boolean empty() {
        return CollectionUtils.isEmpty(this.cidrNotations) && CollectionUtils.isEmpty(this.attributes);
    }

    @JsonIgnore
    public Set<Map.Entry<Map.Entry<String, Integer>, List<PdpAttribute>>> allAttributesGrouped() {
        return this.attributes.stream().collect(groupingBy(attribute -> Map.entry(attribute.name(), attribute.groupID())))
                .entrySet();
    }
}
