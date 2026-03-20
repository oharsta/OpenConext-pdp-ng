package org.openconext.pdp.model;

public record IPInfo(
        String networkAddress,
        String broadcastAddress,
        double capacity,
        boolean ipv4,
        int prefix
) {
}
