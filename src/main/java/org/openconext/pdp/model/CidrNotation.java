package org.openconext.pdp.model;

public record CidrNotation(
        String ipAddress,
        int prefix,
        IPInfo ipInfo
) {
}
