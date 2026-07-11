package com.ondrecreates.incidentmanagement.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Quick-win acceptance: bulk endpoints are per-item, not all-or-nothing --
 * a batch mixing a valid and an invalid transition still applies the valid
 * one and reports the invalid one's failure individually.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BulkOperationApiIntegrationTest {

    private static final String ACTOR_EMAIL = "actor@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void bulkTransitionAppliesValidItemsAndReportsInvalidOnesIndividually() throws Exception {
        Long assignable = createIncident(); // CREATED -> ASSIGNED is valid
        Long notAssignable = createIncident();
        transitionTo(notAssignable, "ASSIGNED"); // now ASSIGNED, so ASSIGNED again is invalid

        Map<String, Object> request = Map.of(
                "incidentIds", List.of(assignable, notAssignable),
                "targetStatus", "ASSIGNED"
        );

        mockMvc.perform(post("/api/v1/incidents/bulk-transition")
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].incidentId").value(assignable))
                .andExpect(jsonPath("$[0].success").value(true))
                .andExpect(jsonPath("$[1].incidentId").value(notAssignable))
                .andExpect(jsonPath("$[1].success").value(false))
                .andExpect(jsonPath("$[1].error").isNotEmpty());

        mockMvc.perform(get("/api/v1/incidents/{id}", assignable).with(jwt()))
                .andExpect(jsonPath("$.incident.status").value("ASSIGNED"));
    }

    @Test
    void bulkAssignSetsAssignedUserOnAllItems() throws Exception {
        Long first = createIncident();
        Long second = createIncident();

        Map<String, Object> request = Map.of(
                "incidentIds", List.of(first, second),
                "assignedUserId", "responder@example.com"
        );

        mockMvc.perform(post("/api/v1/incidents/bulk-assign")
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].success").value(true))
                .andExpect(jsonPath("$[1].success").value(true));

        mockMvc.perform(get("/api/v1/incidents/{id}", first).with(jwt()))
                .andExpect(jsonPath("$.incident.assignedUserId").value("responder@example.com"));
    }

    @Test
    void bulkTransitionReportsFailureForNonExistentIncident() throws Exception {
        Map<String, Object> request = Map.of(
                "incidentIds", List.of(999_999L),
                "targetStatus", "ASSIGNED"
        );

        mockMvc.perform(post("/api/v1/incidents/bulk-transition")
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].success").value(false));
    }

    private Long createIncident() throws Exception {
        Map<String, Object> createRequest = Map.of(
                "title", "Bulk-op target incident",
                "severity", "LOW",
                "priority", "P4"
        );
        String response = mockMvc.perform(post("/api/v1/incidents")
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private void transitionTo(Long incidentId, String targetStatus) throws Exception {
        mockMvc.perform(post("/api/v1/incidents/{id}/transition", incidentId)
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("targetStatus", targetStatus))))
                .andExpect(status().isOk());
    }
}
