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
 * Fáze 2 acceptance: team CRUD + routing an incident to a team is independent
 * of individual assignment (see IncidentApiIntegrationTest for the
 * individual-assignment happy path).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TeamApiIntegrationTest {

    private static final String ACTOR_EMAIL = "lead@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTeamListAndAddMember() throws Exception {
        Map<String, Object> createRequest = Map.of(
                "name", "SRE On-Call",
                "memberEmails", List.of("a@example.com", "b@example.com")
        );

        String createResponse = mockMvc.perform(post("/api/v1/teams")
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("SRE On-Call"))
                .andExpect(jsonPath("$.memberEmails", hasSize(2)))
                .andReturn().getResponse().getContentAsString();

        Long teamId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/v1/teams").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)]".formatted(teamId), hasSize(1)));

        Map<String, Object> addMemberRequest = Map.of("userEmail", "c@example.com");
        mockMvc.perform(post("/api/v1/teams/{id}/members", teamId)
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addMemberRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberEmails", hasSize(3)));

        mockMvc.perform(get("/api/v1/teams/{id}", teamId).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberEmails", hasSize(3)));
    }

    @Test
    void assigningIncidentToTeamWritesTeamAssignmentTimelineEntryAndFilterWorks() throws Exception {
        Map<String, Object> createTeamRequest = Map.of(
                "name", "Payments Squad",
                "memberEmails", List.of("payments-oncall@example.com")
        );
        String teamResponse = mockMvc.perform(post("/api/v1/teams")
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTeamRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long teamId = objectMapper.readTree(teamResponse).get("id").asLong();

        Map<String, Object> createIncidentRequest = Map.of(
                "title", "Payments checkout failing",
                "severity", "HIGH",
                "priority", "P1"
        );
        String incidentResponse = mockMvc.perform(post("/api/v1/incidents")
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createIncidentRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long incidentId = objectMapper.readTree(incidentResponse).get("id").asLong();

        mockMvc.perform(post("/api/v1/incidents/{id}/assign-team", incidentId)
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("teamId", teamId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTeamId").value(teamId));

        mockMvc.perform(get("/api/v1/incidents/{id}/timeline", incidentId).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventType").value("TEAM_ASSIGNMENT"));

        mockMvc.perform(get("/api/v1/incidents").param("assignedTeamId", teamId.toString()).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == %d)]".formatted(incidentId), hasSize(1)));
    }
}
