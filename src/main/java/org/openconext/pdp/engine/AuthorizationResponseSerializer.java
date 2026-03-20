package org.openconext.pdp.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openconext.pdp.model.AuthorizationResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationResponseSerializer {

    private final ObjectMapper objectMapper;

    public AuthorizationResponseSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(AuthorizationResponse response) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize response", e);
        }
    }
}
