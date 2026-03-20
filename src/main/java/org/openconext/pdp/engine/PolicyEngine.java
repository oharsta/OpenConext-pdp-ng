package org.openconext.pdp.engine;

import org.openconext.pdp.model.AuthorizationRequest;
import org.openconext.pdp.model.AuthorizationResponse;

public interface PolicyEngine {

    AuthorizationResponse decide(AuthorizationRequest request);

}
