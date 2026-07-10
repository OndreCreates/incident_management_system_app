package com.ondrecreates.incidentmanagement.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
 * Fáze 2 acceptance: postmortem only allowed once the incident is terminal,
 * exactly one per incident, editable afterwards.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostmortemApiIntegrationTest {

    private static final String ACTOR_EMAIL = "lead@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void rejectsPostmortemForNonTerminalIncident() throws Exception {
        Long incidentId = createIncident();

        mockMvc.perform(post("/api/v1/incidents/{id}/postmortem", incidentId)
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postmortemBody())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("POSTMORTEM_NOT_ALLOWED"));
    }

    @Test
    void createGetUpdatePostmortemForResolvedIncident() throws Exception {
        Long incidentId = createIncident();
        transition(incidentId, "ASSIGNED");
        transition(incidentId, "INVESTIGATING");
        transition(incidentId, "MITIGATED");
        transition(incidentId, "RESOLVED");

        mockMvc.perform(post("/api/v1/incidents/{id}/postmortem", incidentId)
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postmortemBody())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.incidentId").value(incidentId))
                .andExpect(jsonPath("$.impact").value("Checkout unavailable for 45 minutes"));

        mockMvc.perform(post("/api/v1/incidents/{id}/postmortem", incidentId)
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postmortemBody())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("POSTMORTEM_ALREADY_EXISTS"));

        mockMvc.perform(get("/api/v1/incidents/{id}/postmortem", incidentId).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.impact").value("Checkout unavailable for 45 minutes"));

        Map<String, Object> update = Map.of(
                "impact", "Checkout unavailable for 45 minutes (revised estimate)",
                "rootCause", postmortemBody().get("rootCause"),
                "resolution", postmortemBody().get("resolution"),
                "lessonsLearned", postmortemBody().get("lessonsLearned")
        );
        mockMvc.perform(put("/api/v1/incidents/{id}/postmortem", incidentId)
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.impact").value("Checkout unavailable for 45 minutes (revised estimate)"));
    }

    @Test
    void getReturns404WhenNoPostmortemExists() throws Exception {
        Long incidentId = createIncident();

        mockMvc.perform(get("/api/v1/incidents/{id}/postmortem", incidentId).with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("POSTMORTEM_NOT_FOUND"));
    }

    private Long createIncident() throws Exception {
        Map<String, Object> createRequest = Map.of(
                "title", "Checkout returning 500s",
                "severity", "HIGH",
                "priority", "P1"
        );
        String response = mockMvc.perform(post("/api/v1/incidents")
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void transition(Long incidentId, String targetStatus) throws Exception {
        mockMvc.perform(post("/api/v1/incidents/{id}/transition", incidentId)
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetStatus", targetStatus))))
                .andExpect(status().isOk());
    }

    private Map<String, Object> postmortemBody() {
        return Map.of(
                "impact", "Checkout unavailable for 45 minutes",
                "rootCause", "Connection pool exhaustion under load spike",
                "resolution", "Increased pool size, added circuit breaker",
                "lessonsLearned", "Need load testing before Black Friday",
                "actionItems", "Add autoscaling alert at 80% pool usage"
        );
    }
}
