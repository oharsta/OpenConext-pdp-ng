package org.openconext.pdp.api;

import org.openconext.pdp.engine.AuthorizationResponseSerializer;
import org.openconext.pdp.engine.PolicyEngine;
import org.openconext.pdp.model.AuthorizationRequest;
import org.openconext.pdp.model.AuthorizationResponse;
import org.openconext.pdp.model.PdpPolicyDefinition;
import org.openconext.pdp.service.PolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(
        value = {"/pdp/api"},
        headers = {"Content-Type=application/json"},
        produces = {"application/json"})
public class PdpController {

    private static final Logger LOG = LoggerFactory.getLogger(PdpController.class);

    private final PolicyEngine policyEngine;
    private final PolicyService policyService;
    private final AuthorizationResponseSerializer serializer;

    public PdpController(PolicyEngine policyEngine,
                         PolicyService policyService,
                         AuthorizationResponseSerializer serializer) {
        this.policyEngine = policyEngine;
        this.policyService = policyService;
        this.serializer = serializer;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/decide/policy")
    public String decide(@RequestBody AuthorizationRequest payload) {
        policyService.refreshIfNeeded();
        LOG.debug("decide request: {}", payload);
        AuthorizationResponse response = policyEngine.decide(payload);
        LOG.debug("decide response: {}", response);
        return serializer.toJson(response);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/manage/decide")
    public String decideManage(@RequestBody AuthorizationRequest payload) {
        return decide(payload);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/manage/push")
    public void pushPolicies(@RequestBody List<PdpPolicyDefinition> policies) {
        LOG.info("/manage/push");
        policyService.replacePolicies(policies);
        policyService.reload();
    }
}
