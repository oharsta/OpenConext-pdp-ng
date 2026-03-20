package org.openconext.pdp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PdpPolicyDefinition(
        String id,
        String policyId,
        @NotNull @Size(min = 1) String name,
        @NotNull @Size(min = 1) String description,
        List<String> serviceProviderIds,
        boolean serviceProvidersNegated,
        boolean serviceProviderInvalidOrMissing,
        List<String> identityProviderIds,
        String clientId,
        @Valid List<PdpAttribute> attributes,
        @Valid List<LoA> loas,
        String denyAdvice,
        boolean denyRule,
        boolean allAttributesMustMatch,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Date created,
        String userDisplayName,
        String authenticatingAuthorityName,
        int numberOfViolations,
        int numberOfRevisions,
        String denyAdviceNl,
        int revisionNbr,
        boolean activatedSr,
        boolean active,
        boolean actionsAllowed,
        String type,
        Long parentId
) {
    public PdpPolicyDefinition {
        serviceProviderIds = serviceProviderIds == null ? new ArrayList<>() : serviceProviderIds;
        identityProviderIds = identityProviderIds == null ? new ArrayList<>() : identityProviderIds;
        attributes = attributes == null ? new ArrayList<>() : attributes;
        loas = loas == null ? new ArrayList<>() : loas;
    }

    @JsonIgnore
    public List<String> anyIdentityProviders() {
        return CollectionUtils.isEmpty(this.identityProviderIds) ? Collections.emptyList() : List.of("will-iterate-once");
    }

    @JsonIgnore
    public List<String> anyServiceProviders() {
        return CollectionUtils.isEmpty(this.serviceProviderIds) ? Collections.emptyList() : List.of("will-iterate-once");
    }

    @JsonIgnore
    public Set<Map.Entry<Map.Entry<String, Integer>, List<PdpAttribute>>> allAttributesGrouped() {
        return this.attributes.stream().collect(groupingBy(attribute -> Map.entry(attribute.name(), attribute.groupID())))
                .entrySet();
    }

    @JsonIgnore
    public boolean isIdpOnly() {
        return this.identityProviderIds != null && !this.identityProviderIds.isEmpty();
    }

    @JsonIgnore
    public void sortLoas() {
        this.loas.sort(Comparator.comparing(LoA::level).reversed());
    }

    @JsonIgnore
    public void sortAttributes() {
        this.attributes.sort(Comparator.comparing(PdpAttribute::name));
    }
}
