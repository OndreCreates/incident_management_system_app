package com.ondrecreates.incidentmanagement.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
 * Integrační test proti reálné MySQL (docker-compose service) — happy path
 * celého CRUD flow + 409 na nevalidní přechod, viz FAZE_1_PROMPT.md Fáze 1C.
 * Každý test běží ve vlastní transakci, která se na konci rollbackne.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IncidentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullHappyPathCreateAssignInvestigateComment() throws Exception {
        Map<String, Object> createRequest = Map.of(
                "title", "Payment API returning 500s",
                "description", "Elevated error rate on /payments",
                "severity", "CRITICAL",
                "priority", "P1",
                "createdBy", 100L
        );

        String createResponse = mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.slaBreached").value(false))
                .andReturn().getResponse().getContentAsString();

        Long incidentId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty());

        mockMvc.perform(get("/api/v1/incidents/{id}", incidentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incident.id").value(incidentId))
                .andExpect(jsonPath("$.timeline").isEmpty());

        Map<String, Object> assignTransition = Map.of(
                "targetStatus", "ASSIGNED",
                "note", "Assigning to on-call",
                "assignedUserId", 200L,
                "actorUserId", 100L
        );
        mockMvc.perform(post("/api/v1/incidents/{id}/transition", incidentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignTransition)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedUserId").value(200));

        Map<String, Object> investigateTransition = Map.of(
                "targetStatus", "INVESTIGATING",
                "actorUserId", 200L
        );
        mockMvc.perform(post("/api/v1/incidents/{id}/transition", incidentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(investigateTransition)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INVESTIGATING"));

        Map<String, Object> comment = Map.of(
                "content", "Root cause looks like a DB connection pool exhaustion",
                "authorUserId", 200L
        );
        mockMvc.perform(post("/api/v1/incidents/{id}/comments", incidentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(comment.get("content")));

        mockMvc.perform(get("/api/v1/incidents/{id}/timeline", incidentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].eventType").value("ASSIGNMENT"))
                .andExpect(jsonPath("$[1].eventType").value("STATUS_CHANGE"))
                .andExpect(jsonPath("$[1].toStatus").value("ASSIGNED"))
                .andExpect(jsonPath("$[2].eventType").value("STATUS_CHANGE"))
                .andExpect(jsonPath("$[2].toStatus").value("INVESTIGATING"))
                .andExpect(jsonPath("$[3].eventType").value("COMMENT"))
                .andExpect(jsonPath("$[3].commentId").isNotEmpty());
    }

    @Test
    void invalidTransitionReturns409WithAllowedList() throws Exception {
        Map<String, Object> createRequest = Map.of(
                "title", "Disk usage critical on db-primary",
                "severity", "HIGH",
                "priority", "P2",
                "createdBy", 100L
        );

        String createResponse = mockMvc.perform(post("/api/v1/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long incidentId = objectMapper.readTree(createResponse).get("id").asLong();

        Map<String, Object> invalidTransition = Map.of(
                "targetStatus", "RESOLVED",
                "actorUserId", 100L
        );

        mockMvc.perform(post("/api/v1/incidents/{id}/transition", incidentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTransition)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_TRANSITION"))
                .andExpect(jsonPath("$.from").value("CREATED"))
                .andExpect(jsonPath("$.attempted").value("RESOLVED"))
                .andExpect(jsonPath("$.allowed", hasSize(1)))
                .andExpect(jsonPath("$.allowed[0]").value("ASSIGNED"));
    }
}
