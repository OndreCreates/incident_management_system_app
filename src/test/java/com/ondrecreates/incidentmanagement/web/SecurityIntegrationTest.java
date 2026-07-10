package com.ondrecreates.incidentmanagement.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fáze 1D acceptance: mutující i čtecí /api/v1/** endpointy vyžadují validní
 * JWT. /actuator/health zůstává otevřený pro lokální vývoj.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void mutatingEndpointWithoutTokenReturns401() throws Exception {
        Map<String, Object> createRequest = Map.of(
                "title", "Unauthenticated attempt",
                "severity", "LOW",
                "priority", "P4"
        );

        mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void readEndpointWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void mutatingEndpointWithValidTokenReturns201() throws Exception {
        Map<String, Object> createRequest = Map.of(
                "title", "Authenticated attempt",
                "severity", "LOW",
                "priority", "P4"
        );

        mockMvc.perform(post("/api/v1/incidents")
                        .with(jwt().jwt(builder -> builder.subject("authenticated@example.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void healthEndpointIsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void swaggerUiIsPubliclyAccessible() throws Exception {
        // /swagger-ui.html is springdoc's own redirect entry point to /swagger-ui/index.html --
        // distinct from the /swagger-ui/** pattern, easy to leave unlisted by accident.
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void apiDocsArePubliclyAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
