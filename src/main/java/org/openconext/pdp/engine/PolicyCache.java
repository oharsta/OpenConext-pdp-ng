package org.openconext.pdp.engine;

import org.openconext.pdp.model.PdpPolicyDefinition;

import java.util.List;

public record PolicyCache(
        List<PdpPolicyDefinition> policies,
        long version
) {
}
