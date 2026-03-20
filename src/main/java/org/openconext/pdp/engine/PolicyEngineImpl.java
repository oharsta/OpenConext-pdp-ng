package org.openconext.pdp.engine;

import org.openconext.pdp.model.AssociatedAdvice;
import org.openconext.pdp.model.AttributeAssignment;
import org.openconext.pdp.model.AuthorizationRequest;
import org.openconext.pdp.model.AuthorizationResponse;
import org.openconext.pdp.model.Decision;
import org.openconext.pdp.model.Obligation;
import org.openconext.pdp.model.PolicyIdReference;
import org.openconext.pdp.model.PolicyIdentifier;
import org.openconext.pdp.model.PolicyMatchResult;
import org.openconext.pdp.model.ResponseResult;
import org.openconext.pdp.model.Status;
import org.openconext.pdp.model.StatusCode;
import org.openconext.pdp.repository.PolicyCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PolicyEngineImpl implements PolicyEngine {

    private static final Logger LOG = LoggerFactory.getLogger(PolicyEngineImpl.class);
    private static final String ROOT_POLICY_SET_ID = "urn:openconext:pdp:root:policyset";
    private static final String STATUS_OK = "urn:oasis:names:tc:xacml:1.0:status:ok";
    private static final String STATUS_MISSING = "urn:oasis:names:tc:xacml:1.0:status:missing-attribute";
    private static final String STATUS_MISSING_MESSAGE = "Missing required attribute";
    private static final String STATUS_MISSING_DETAIL = "<MissingAttributeDetail Category=\\\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\\\" AttributeId=\\\"%s\\\" DataType=\\\"http://www.w3.org/2001/XMLSchema#string\\\"></MissingAttributeDetail>";
    private static final String OBLIGATION_ID = "urn:openconext:stepup:loa";
    private static final String OBLIGATION_CATEGORY = "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject";
    private static final String OBLIGATION_ATTRIBUTE_ID = "urn:loa:level";
    private static final String DATA_TYPE_STRING = "http://www.w3.org/2001/XMLSchema#string";
    private static final String ADVICE_CATEGORY = "urn:oasis:names:tc:xacml:3.0:attribute-category:resource";

    private final PolicyCacheRepository policyCacheRepository;
    private final PolicyEvaluator policyEvaluator = new PolicyEvaluator();

    public PolicyEngineImpl(PolicyCacheRepository policyCacheRepository) {
        this.policyCacheRepository = policyCacheRepository;
    }

    @Override
    public AuthorizationResponse decide(AuthorizationRequest request) {
        PolicyCache cache = policyCacheRepository.getCache();
        List<PolicyMatchResult> permitMatches = new ArrayList<>();
        PolicyMatchResult firstPermit = null;
        PolicyMatchResult firstDeny = null;
        PolicyMatchResult firstIndeterminate = null;
        PolicyMatchResult firstNotApplicable = null;
        for (var policy : cache.policies()) {
            PolicyMatchResult result = policyEvaluator.evaluate(policy, request);
            if (result.decision() == Decision.Indeterminate) {
                if (firstIndeterminate == null) {
                    firstIndeterminate = result;
                }
                continue;
            }
            if (result.decision() == Decision.NotApplicable) {
                if (firstNotApplicable == null) {
                    firstNotApplicable = result;
                }
                continue;
            }
            if (!result.matched()) {
                continue;
            }
            if (result.decision() == Decision.Deny) {
                if (firstDeny == null) {
                    firstDeny = result;
                }
            } else if (result.decision() == Decision.Permit) {
                permitMatches.add(result);
                if (firstPermit == null) {
                    firstPermit = result;
                }
            }
        }

        ResponseResult responseResult;
        Status status;

        if (firstDeny != null) {
            responseResult = new ResponseResult(
                    statusWithOk(),
                    List.of(),
                    List.of(buildAdvice(firstDeny)),
                    firstDeny.categories().isEmpty() ? List.of() : firstDeny.categories(),
                    buildPolicyIdentifier(firstDeny),
                    Decision.Deny.name()
            );
            return wrap(responseResult);
        }

        if (firstIndeterminate != null) {
            responseResult = new ResponseResult(
                    buildMissingStatus(firstIndeterminate),
                    List.of(),
                    List.of(),
                    List.of(),
                    buildPolicyIdentifier(firstIndeterminate),
                    Decision.Indeterminate.name()
            );
            return wrap(responseResult);
        }

        if (permitMatches.isEmpty()) {
            if (firstNotApplicable != null) {
                responseResult = new ResponseResult(
                        statusWithOk(),
                        List.of(),
                        List.of(),
                        List.of(),
                        buildPolicySetIdentifier(),
                        Decision.NotApplicable.name()
                );
                return wrap(responseResult);
            }
            status = new Status(new StatusCode(STATUS_OK), null, null);
            responseResult = new ResponseResult(
                    status,
                    List.of(),
                    List.of(),
                    List.of(),
                    null,
                    Decision.Deny.name()
            );
            return wrap(responseResult);
        }

        PolicyMatchResult first = firstPermit;
        status = new Status(new StatusCode(STATUS_OK), null, null);
        responseResult = new ResponseResult(
                status,
                first.loaLevel() != null ? List.of(buildObligation(first.loaLevel())) : List.of(),
                List.of(),
                first.categories().isEmpty() ? List.of() : first.categories(),
                buildPolicyIdentifier(permitMatches),
                Decision.Permit.name()
        );

        LOG.info("Decision {} for {} policies", responseResult.decision(), permitMatches.size());
        return wrap(responseResult);
    }

    private AuthorizationResponse wrap(ResponseResult result) {
        return new AuthorizationResponse(List.of(result));
    }

    private Status statusWithOk() {
        return new Status(new StatusCode(STATUS_OK), null, null);
    }

    private Status buildMissingStatus(PolicyMatchResult result) {
        StatusCode statusCode = new StatusCode(STATUS_MISSING);
        String detail = result.missingAttributeId() != null
                ? STATUS_MISSING_DETAIL.formatted(result.missingAttributeId())
                : null;
        return new Status(statusCode, STATUS_MISSING_MESSAGE, detail);
    }

    private PolicyIdentifier buildPolicyIdentifier(PolicyMatchResult match) {
        List<PolicyIdReference> policySetIds = List.of(new PolicyIdReference("1.0", ROOT_POLICY_SET_ID));
        List<PolicyIdReference> policyIds = match.policyId() != null
                ? List.of(new PolicyIdReference("1", match.policyId()))
                : List.of();
        return new PolicyIdentifier(policySetIds, policyIds);
    }

    private PolicyIdentifier buildPolicyIdentifier(List<PolicyMatchResult> matches) {
        List<PolicyIdReference> policyIds = new ArrayList<>();
        for (PolicyMatchResult match : matches) {
            if (match.policyId() != null) {
                policyIds.add(new PolicyIdReference("1", match.policyId()));
            }
        }
        List<PolicyIdReference> policySetIds = List.of(new PolicyIdReference("1.0", ROOT_POLICY_SET_ID));
        return new PolicyIdentifier(policySetIds, policyIds);
    }

    private PolicyIdentifier buildPolicySetIdentifier() {
        List<PolicyIdReference> policySetIds = List.of(new PolicyIdReference("1.0", ROOT_POLICY_SET_ID));
        return new PolicyIdentifier(policySetIds, List.of());
    }

    private Obligation buildObligation(String loa) {
        AttributeAssignment assignment = new AttributeAssignment(
                OBLIGATION_CATEGORY,
                OBLIGATION_ATTRIBUTE_ID,
                loa,
                DATA_TYPE_STRING
        );
        return new Obligation(List.of(assignment), OBLIGATION_ID);
    }

    private AssociatedAdvice buildAdvice(PolicyMatchResult match) {
        List<AttributeAssignment> assignments = new ArrayList<>();

        assignments.add(new AttributeAssignment(
                ADVICE_CATEGORY,
                "DenyMessage:en",
                match.denyAdvice(),
                DATA_TYPE_STRING
        ));

        assignments.add(new AttributeAssignment(
                ADVICE_CATEGORY,
                "DenyMessage:nl",
                match.denyAdviceNl(),
                DATA_TYPE_STRING
        ));

        assignments.add(new AttributeAssignment(
                ADVICE_CATEGORY,
                "IdPOnly",
                match.idpOnly(),
                "http://www.w3.org/2001/XMLSchema#boolean"
        ));

        return new AssociatedAdvice(assignments, match.policyId());
    }
}
