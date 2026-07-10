package com.ondrecreates.incidentmanagement.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
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
 *
 * Autentizace se simuluje přes SecurityMockMvcRequestPostProcessors.jwt() —
 * ověřuje naši vlastní autorizační/extrakční logiku (actorUserId z sub
 * claimu). Ověření proti skutečně vydanému tokenu z identity_server_app je
 * záměrně mimo automatizovanou sadu — jde o plný authorization_code + PKCE
 * flow s přihlašovací stránkou, který pokrývá manuální E2E krok ve Fázi 1G.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IncidentApiIntegrationTest {

    private static final String CREATOR_EMAIL = "creator@example.com";
    private static final String RESPONDER_EMAIL = "responder@example.com";

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
                "priority", "P1"
        );

        String createResponse = mockMvc.perform(post("/api/v1/incidents")
                        .with(jwt().jwt(builder -> builder.subject(CREATOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.createdBy").value(CREATOR_EMAIL))
                .andExpect(jsonPath("$.slaBreached").value(false))
                .andReturn().getResponse().getContentAsString();

        Long incidentId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/v1/incidents").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty());

        mockMvc.perform(get("/api/v1/incidents/{id}", incidentId).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incident.id").value(incidentId))
                .andExpect(jsonPath("$.timeline").isEmpty());

        Map<String, Object> assignTransition = Map.of(
                "targetStatus", "ASSIGNED",
                "note", "Assigning to on-call",
                "assignedUserId", RESPONDER_EMAIL
        );
        mockMvc.perform(post("/api/v1/incidents/{id}/transition", incidentId)
                        .with(jwt().jwt(builder -> builder.subject(CREATOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignTransition)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"))
                .andExpect(jsonPath("$.assignedUserId").value(RESPONDER_EMAIL));

        Map<String, Object> investigateTransition = Map.of(
                "targetStatus", "INVESTIGATING"
        );
        mockMvc.perform(post("/api/v1/incidents/{id}/transition", incidentId)
                        .with(jwt().jwt(builder -> builder.subject(RESPONDER_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(investigateTransition)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INVESTIGATING"));

        Map<String, Object> comment = Map.of(
                "content", "Root cause looks like a DB connection pool exhaustion"
        );
        mockMvc.perform(post("/api/v1/incidents/{id}/comments", incidentId)
                        .with(jwt().jwt(builder -> builder.subject(RESPONDER_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(comment.get("content")))
                .andExpect(jsonPath("$.authorUserId").value(RESPONDER_EMAIL));

        mockMvc.perform(get("/api/v1/incidents/{id}/timeline", incidentId).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].eventType").value("ASSIGNMENT"))
                .andExpect(jsonPath("$[1].eventType").value("STATUS_CHANGE"))
                .andExpect(jsonPath("$[1].toStatus").value("ASSIGNED"))
                .andExpect(jsonPath("$[2].eventType").value("STATUS_CHANGE"))
                .andExpect(jsonPath("$[2].toStatus").value("INVESTIGATING"))
                .andExpect(jsonPath("$[3].eventType").value("COMMENT"))
                .andExpect(jsonPath("$[3].commentId").isNotEmpty())
                .andExpect(jsonPath("$[3].commentContent").value(comment.get("content")));
    }

    @Test
    void invalidTransitionReturns409WithAllowedList() throws Exception {
        Map<String, Object> createRequest = Map.of(
                "title", "Disk usage critical on db-primary",
                "severity", "HIGH",
                "priority", "P2"
        );

        String createResponse = mockMvc.perform(post("/api/v1/incidents")
                        .with(jwt().jwt(builder -> builder.subject(CREATOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long incidentId = objectMapper.readTree(createResponse).get("id").asLong();

        Map<String, Object> invalidTransition = Map.of(
                "targetStatus", "RESOLVED"
        );

        mockMvc.perform(post("/api/v1/incidents/{id}/transition", incidentId)
                        .with(jwt().jwt(builder -> builder.subject(CREATOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTransition)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("INVALID_TRANSITION"))
                .andExpect(jsonPath("$.from").value("CREATED"))
                .andExpect(jsonPath("$.attempted").value("RESOLVED"))
                .andExpect(jsonPath("$.allowed", hasSize(1)))
                .andExpect(jsonPath("$.allowed[0]").value("ASSIGNED"));
    }

    @Test
    void searchQueryMatchesTitleOrDescriptionCaseInsensitively() throws Exception {
        mockMvc.perform(post("/api/v1/incidents")
                        .with(jwt().jwt(builder -> builder.subject(CREATOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Checkout payment gateway timeout",
                                "severity", "HIGH", "priority", "P1"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/incidents")
                        .with(jwt().jwt(builder -> builder.subject(CREATOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Unrelated login page glitch",
                                "severity", "LOW", "priority", "P4"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/incidents").param("q", "PAYMENT GATEWAY").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Checkout payment gateway timeout"));
    }
}
