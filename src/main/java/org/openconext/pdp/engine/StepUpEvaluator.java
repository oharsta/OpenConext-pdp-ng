package org.openconext.pdp.engine;

import org.openconext.pdp.model.CidrNotation;
import org.openconext.pdp.model.Decision;
import org.openconext.pdp.model.LoA;
import org.openconext.pdp.model.PdpAttribute;
import org.openconext.pdp.model.PdpPolicyDefinition;
import org.openconext.pdp.model.PolicyMatchResult;
import org.openconext.pdp.model.RequestAttribute;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StepUpEvaluator {

    private final ConditionMatcher conditionMatcher;

    public StepUpEvaluator(ConditionMatcher conditionMatcher) {
        this.conditionMatcher = conditionMatcher;
    }

    public PolicyMatchResult evaluate(PdpPolicyDefinition policy, PolicyEvaluator.RequestContext context, PolicyMatchResult result) {
        if (policy.loas() == null || policy.loas().isEmpty()) {
            return result;
        }
        Map<String, List<RequestAttribute>> subject = context.getSubjectAttributesMap();
        for (LoA loa : policy.loas()) {
            if (matchesLoa(loa, subject)) {
                return new PolicyMatchResult(
                        Decision.Permit,
                        true,
                        result.indeterminate(),
                        result.policyId(),
                        result.denyAdvice(),
                        result.denyAdviceNl(),
                        result.idpOnly(),
                        loa.level(),
                        result.missingAttributeId(),
                        result.categories()
                );
            }
        }
        return new PolicyMatchResult(
                Decision.Permit,
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

    private boolean matchesLoa(LoA loa, Map<String, List<RequestAttribute>> subject) {
        boolean attributesMatch = matchAttributes(loa, subject);
        boolean cidrMatch = matchCidr(loa, subject);
        if (loa.attributes().isEmpty() && !loa.cidrNotations().isEmpty()) {
            return cidrMatch;
        }
        if (loa.cidrNotations().isEmpty() && !loa.attributes().isEmpty()) {
            return attributesMatch;
        }
        return attributesMatch && cidrMatch;
    }

    private boolean matchAttributes(LoA loa, Map<String, List<RequestAttribute>> subject) {
        if (loa.attributes() == null || loa.attributes().isEmpty()) {
            return true;
        }
        Map<String, List<PdpAttribute>> grouped = loa.attributes().stream()
                .collect(Collectors.groupingBy(PdpAttribute::name));

        for (Map.Entry<String, List<PdpAttribute>> entry : grouped.entrySet()) {
            List<RequestAttribute> requestValues = subject.getOrDefault(entry.getKey(), List.of());
            if (requestValues.isEmpty()) {
                if (loa.allAttributesMustMatch()) {
                    return false;
                }
                continue;
            }
            boolean groupMatch = false;
            for (PdpAttribute policyAttribute : entry.getValue()) {
                if (conditionMatcher.matches(policyAttribute, requestValues)) {
                    groupMatch = true;
                    break;
                }
            }
            if (loa.allAttributesMustMatch() && !groupMatch) {
                return false;
            }
            if (!loa.allAttributesMustMatch() && groupMatch) {
                return true;
            }
        }
        return loa.allAttributesMustMatch();
    }

    private boolean matchCidr(LoA loa, Map<String, List<RequestAttribute>> subject) {
        if (loa.cidrNotations() == null || loa.cidrNotations().isEmpty()) {
            return true;
        }
        List<RequestAttribute> ipAttributes = subject.getOrDefault("urn:mace:surfnet.nl:collab:xacml-attribute:ip-address", List.of());
        if (ipAttributes.isEmpty()) {
            return false;
        }
        for (RequestAttribute ipAttribute : ipAttributes) {
            if (ipAttribute.value() == null) {
                continue;
            }
            boolean inRange = false;
            for (CidrNotation cidr : loa.cidrNotations()) {
                if (isInRange(ipAttribute.value(), cidr.ipAddress(), cidr.prefix())) {
                    inRange = true;
                    break;
                }
            }
            if (loa.negateCidrNotation()) {
                if (!inRange) {
                    return true;
                }
            } else if (inRange) {
                return true;
            }
        }
        return false;
    }

    private boolean isInRange(String ip, String cidrIp, int prefix) {
        try {
            InetAddress ipAddr = InetAddress.getByName(ip);
            InetAddress cidrAddr = InetAddress.getByName(cidrIp);
            byte[] ipBytes = ipAddr.getAddress();
            byte[] cidrBytes = cidrAddr.getAddress();
            if (ipBytes.length != cidrBytes.length) {
                return false;
            }
            BigInteger ipVal = new BigInteger(1, ipBytes);
            BigInteger cidrVal = new BigInteger(1, cidrBytes);
            int maxBits = ipBytes.length * 8;
            BigInteger mask = BigInteger.ONE.shiftLeft(maxBits).subtract(BigInteger.ONE)
                    .xor(BigInteger.ONE.shiftLeft(maxBits - prefix).subtract(BigInteger.ONE));
            return ipVal.and(mask).equals(cidrVal.and(mask));
        } catch (Exception e) {
            return false;
        }
    }
}
