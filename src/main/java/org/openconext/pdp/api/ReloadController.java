package org.openconext.pdp.api;

import org.openconext.pdp.service.PolicyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/pdp/api"})
public class ReloadController {

    private final PolicyService policyService;

    public ReloadController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping("/manage/reload")
    public void reload() {
        policyService.reload();
    }
}
