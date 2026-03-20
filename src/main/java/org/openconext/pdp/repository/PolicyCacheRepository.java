package org.openconext.pdp.repository;

import org.openconext.pdp.engine.PolicyCache;

public interface PolicyCacheRepository {
    PolicyCache getCache();

    void reload();
}
