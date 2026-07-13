package com.ondrecreates.incidentmanagement.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ondrecreates.incidentmanagement.domain.AppUserRole;
import com.ondrecreates.incidentmanagement.domain.Role;
import com.ondrecreates.incidentmanagement.repository.AppUserRoleRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fáze 3 acceptance: SLA policy is admin-editable and only affects incidents
 * created after the change -- already-open incidents keep their originally
 * computed deadline (computed once at creation, never recalculated).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SlaPolicyApiIntegrationTest {

    private static final String ACTOR_EMAIL = "admin@example.com";
    private static final String MEMBER_EMAIL = "member@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRoleRepository roleRepository;

    @BeforeEach
    void seedAdmin() {
        roleRepository.save(new AppUserRole(ACTOR_EMAIL, Role.ADMIN));
    }

    @Test
    void listReturnsAllFourSeverityPolicies() throws Exception {
        mockMvc.perform(get("/api/v1/sla-policies").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void nonAdminCannotUpdatePolicy() throws Exception {
        mockMvc.perform(put("/api/v1/sla-policies/LOW")
                        .with(jwt().jwt(builder -> builder.subject(MEMBER_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("slaMinutes", 30, "nearBreachPercentage", 80))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ROLE_FORBIDDEN"));
    }

    @Test
    void updatingPolicyAffectsOnlyIncidentsCreatedAfterwards() throws Exception {
        Long existingIncidentId = createIncident();
        String beforeUpdate = fetchIncidentJson(existingIncidentId);
        String originalDeadline = objectMapper.readTree(beforeUpdate).get("incident").get("slaDeadline").asText();

        mockMvc.perform(put("/api/v1/sla-policies/LOW")
                        .with(jwt().jwt(builder -> builder.subject(ACTOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("slaMinutes", 30, "nearBreachPercentage", 80))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slaMinutes").value(30));

        String afterUpdate = fetchIncidentJson(existingIncidentId);
        assertThat(objectMapper.readTree(afterUpdate).get("incident").get("slaDeadline").asText())
                .isEqualTo(originalDeadline);

        Long newIncidentId = createIncident();
        String newIncidentJson = fetchIncidentJson(newIncidentId);
        Instant newCreatedAt = Instant.parse(objectMapper.readTree(newIncidentJson).get("incident").get("createdAt").asText());
        Instant newDeadline = Instant.parse(objectMapper.readTree(newIncidentJson).get("incident").get("slaDeadline").asText());
        long minutesUntilDeadline = Duration.between(newCreatedAt, newDeadline).toMinutes();

        assertThat(minutesUntilDeadline).isBetween(28L, 32L);
    }

    private Long createIncident() throws Exception {
        Map<String, Object> createRequest = Map.of(
                "title", "Something LOW severity",
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

    private String fetchIncidentJson(Long incidentId) throws Exception {
        return mockMvc.perform(get("/api/v1/incidents/{id}", incidentId).with(jwt()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }
}
