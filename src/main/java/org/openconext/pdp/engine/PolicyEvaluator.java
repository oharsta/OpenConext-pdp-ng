package org.openconext.pdp.engine;

import org.openconext.pdp.model.AuthorizationRequest;
import org.openconext.pdp.model.Category;
import org.openconext.pdp.model.CategoryAttribute;
import org.openconext.pdp.model.Decision;
import org.openconext.pdp.model.PdpAttribute;
import org.openconext.pdp.model.PdpPolicyDefinition;
import org.openconext.pdp.model.PolicyMatchResult;
import org.openconext.pdp.model.RequestAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PolicyEvaluator {

    private final ConditionMatcher conditionMatcher = new ConditionMatcher();

    public PolicyMatchResult evaluate(PdpPolicyDefinition policy, AuthorizationRequest request) {
        PolicyMatchResult result = new PolicyMatchResult(
                Decision.Deny,
                false,
                false,
                policy.policyId(),
                policy.denyAdvice(),
                policy.denyAdviceNl(),
                policy.isIdpOnly(),
                null,
                null,
                new ArrayList<>()
        );

        RequestContext context = RequestContext.from(request);

        if (!policy.active()) {
            return result;
        }

        if (!matchesServiceProvider(policy, context)) {
            return new PolicyMatchResult(
                    policy.serviceProvidersNegated() ? Decision.Permit : Decision.NotApplicable,
                    true,
                    result.indeterminate(),
                    result.policyId(),
                    result.denyAdvice(),
                    result.denyAdviceNl(),
                    result.idpOnly(),
                    result.loaLevel(),
                    result.missingAttributeId(),
                    result.categories()
            );
        }

        if (!matchesIdentityProvider(policy, context)) {
            return new PolicyMatchResult(
                    Decision.NotApplicable,
                    result.matched(),
                    result.indeterminate(),
                    result.policyId(),
                    result.denyAdvice(),
                    result.denyAdviceNl(),
                    result.idpOnly(),
                    result.loaLevel(),
                    result.missingAttributeId(),
                    result.categories()
            );
        }

        if (policy.attributes() != null && !policy.attributes().isEmpty()) {
            AttributeEvaluation evaluation = evaluateAttributes(policy, context, result);
            result = evaluation.result();
            if (evaluation.outcome() == AttributeMatchOutcome.INDETERMINATE) {
                return result;
            }
            if (evaluation.outcome() == AttributeMatchOutcome.NOT_MATCHED) {
                Decision decision = policy.serviceProvidersNegated() ? Decision.Deny : (policy.denyRule() ? Decision.Permit : Decision.Deny);
                return new PolicyMatchResult(
                        decision,
                        true,
                        result.indeterminate(),
                        result.policyId(),
                        result.denyAdvice(),
                        result.denyAdviceNl(),
                        result.idpOnly(),
                        result.loaLevel(),
                        result.missingAttributeId(),
                        result.categories()
                );
            }
            return new PolicyMatchResult(
                    policy.denyRule() ? Decision.Deny : Decision.Permit,
                    true,
                    result.indeterminate(),
                    result.policyId(),
                    result.denyAdvice(),
                    result.denyAdviceNl(),
                    result.idpOnly(),
                    result.loaLevel(),
                    result.missingAttributeId(),
                    result.categories()
            );
        }

        if (isStepPolicy(policy)) {
            StepUpEvaluator stepUpEvaluator = new StepUpEvaluator(conditionMatcher);
            return stepUpEvaluator.evaluate(policy, context, result);
        }

        return new PolicyMatchResult(
                policy.denyRule() ? Decision.Deny : Decision.Permit,
                true,
                result.indeterminate(),
                result.policyId(),
                result.denyAdvice(),
                result.denyAdviceNl(),
                result.idpOnly(),
                result.loaLevel(),
                result.missingAttributeId(),
                result.categories()
        );
    }

    private boolean isStepPolicy(PdpPolicyDefinition policy) {
        return policy.type() != null && policy.type().toLowerCase(Locale.ROOT).equals("step");
    }

    private boolean matchesServiceProvider(PdpPolicyDefinition policy, RequestContext context) {
        List<String> spIds = policy.serviceProviderIds();
        if (spIds == null || spIds.isEmpty()) {
            return true;
        }
        boolean match = spIds.contains(context.getServiceProvider());
        if (policy.serviceProvidersNegated()) {
            return !match;
        }
        return match;
    }

    private boolean matchesIdentityProvider(PdpPolicyDefinition policy, RequestContext context) {
        List<String> idpIds = policy.identityProviderIds();
        if (idpIds == null || idpIds.isEmpty()) {
            return true;
        }
        return idpIds.contains(context.getIdentityProvider());
    }

    private AttributeEvaluation evaluateAttributes(PdpPolicyDefinition policy,
                                                   RequestContext context,
                                                   PolicyMatchResult result) {
        List<PdpAttribute> attributes = policy.attributes();
        boolean allMustMatch = policy.allAttributesMustMatch();
        boolean denyRule = policy.denyRule();
        Map<String, List<PdpAttribute>> grouped = attributes.stream()
                .collect(Collectors.groupingBy(PdpAttribute::name));

        List<Category> categories = new ArrayList<>();
        for (Map.Entry<String, List<PdpAttribute>> entry : grouped.entrySet()) {
            String attributeId = entry.getKey();
            List<RequestAttribute> requestValues = context.getSubjectAttributes(attributeId);
            if (requestValues.isEmpty()) {
                if (allMustMatch || denyRule) {
                    PolicyMatchResult updated = new PolicyMatchResult(
                            Decision.Indeterminate,
                            result.matched(),
                            true,
                            policy.policyId(),
                            result.denyAdvice(),
                            result.denyAdviceNl(),
                            result.idpOnly(),
                            result.loaLevel(),
                            attributeId,
                            result.categories()
                    );
                    return new AttributeEvaluation(updated, AttributeMatchOutcome.INDETERMINATE);
                }
                continue;
            }
            boolean groupMatch = false;
            String matchedValue = null;
            for (PdpAttribute policyAttribute : entry.getValue()) {
                String matched = conditionMatcher.findMatchedValue(policyAttribute, requestValues);
                if (matched != null) {
                    groupMatch = true;
                    matchedValue = matched;
                    break;
                }
            }
            if (groupMatch) {
                CategoryAttribute categoryAttribute = new CategoryAttribute(
                        attributeId,
                        matchedValue,
                        "http://www.w3.org/2001/XMLSchema#string"
                );
                Category category = new Category(attributeId, List.of(categoryAttribute));
                if (allMustMatch) {
                    categories.clear();
                    categories.add(category);
                } else if (grouped.size() == 1) {
                    categories.add(category);
                }
            }
            if (allMustMatch && !groupMatch) {
                return new AttributeEvaluation(result, AttributeMatchOutcome.NOT_MATCHED);
            }
            if (!allMustMatch && groupMatch) {
                PolicyMatchResult updated = new PolicyMatchResult(
                        result.decision(),
                        result.matched(),
                        result.indeterminate(),
                        result.policyId(),
                        result.denyAdvice(),
                        result.denyAdviceNl(),
                        result.idpOnly(),
                        result.loaLevel(),
                        result.missingAttributeId(),
                        categories
                );
                return new AttributeEvaluation(updated, AttributeMatchOutcome.MATCHED);
            }
        }

        PolicyMatchResult updated = new PolicyMatchResult(
                result.decision(),
                result.matched(),
                result.indeterminate(),
                result.policyId(),
                result.denyAdvice(),
                result.denyAdviceNl(),
                result.idpOnly(),
                result.loaLevel(),
                result.missingAttributeId(),
                categories
        );
        return new AttributeEvaluation(updated, allMustMatch ? AttributeMatchOutcome.MATCHED : AttributeMatchOutcome.NOT_MATCHED);
    }

    private record AttributeEvaluation(PolicyMatchResult result, AttributeMatchOutcome outcome) {
    }

    private enum AttributeMatchOutcome {
        MATCHED,
        NOT_MATCHED,
        INDETERMINATE
    }

    static class RequestContext {
        private final Map<String, List<RequestAttribute>> subjectAttributes;
        private final String serviceProvider;
        private final String identityProvider;

        RequestContext(Map<String, List<RequestAttribute>> subjectAttributes, String serviceProvider, String identityProvider) {
            this.subjectAttributes = subjectAttributes;
            this.serviceProvider = serviceProvider;
            this.identityProvider = identityProvider;
        }

        static RequestContext from(AuthorizationRequest request) {
            List<RequestAttribute> subject = request.request().accessSubject() != null
                    ? request.request().accessSubject().attribute()
                    : List.of();
            Map<String, List<RequestAttribute>> subjectMap = subject.stream()
                    .collect(Collectors.groupingBy(RequestAttribute::attributeId));
            List<RequestAttribute> resource = request.request().resource() != null
                    ? request.request().resource().attribute()
                    : List.of();
            Map<String, RequestAttribute> resourceMap = resource.stream()
                    .collect(Collectors.toMap(RequestAttribute::attributeId, Function.identity(), (a, b) -> a));
            String sp = resourceMap.getOrDefault("SPentityID", new RequestAttribute(null, null)).value();
            String idp = resourceMap.getOrDefault("IDPentityID", new RequestAttribute(null, null)).value();
            return new RequestContext(subjectMap, sp, idp);
        }

        List<RequestAttribute> getSubjectAttributes(String attributeId) {
            return subjectAttributes.getOrDefault(attributeId, List.of());
        }

        Map<String, List<RequestAttribute>> getSubjectAttributesMap() {
            return subjectAttributes;
        }

        public String getServiceProvider() {
            return serviceProvider;
        }

        public String getIdentityProvider() {
            return identityProvider;
        }
    }
}
