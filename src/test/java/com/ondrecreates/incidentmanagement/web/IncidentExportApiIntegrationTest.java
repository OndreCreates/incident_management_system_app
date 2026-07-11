package com.ondrecreates.incidentmanagement.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
 * Quick-win acceptance: CSV export respects the same filters as the list
 * endpoint and returns the full matching set, unpaginated.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IncidentExportApiIntegrationTest {

    private static final String CREATOR_EMAIL = "creator@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void exportReturnsCsvWithHeaderAndMatchingIncidents() throws Exception {
        createIncident("CSV export target incident", "CRITICAL", "P1");
        createIncident("Unrelated incident", "LOW", "P4");

        String csv = mockMvc.perform(get("/api/v1/incidents/export").param("q", "CSV export").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"incidents.csv\""))
                .andReturn().getResponse().getContentAsString();

        String[] lines = csv.split("\r\n");
        assertThat(lines[0]).isEqualTo(
                "id,title,status,severity,priority,assignedUserId,assignedTeamId,slaDeadline,slaBreached,"
                        + "createdBy,createdAt,updatedAt");
        assertThat(lines).hasSize(2);
        assertThat(lines[1]).contains("CSV export target incident").contains("CRITICAL").contains("P1");
    }

    private void createIncident(String title, String severity, String priority) throws Exception {
        Map<String, Object> createRequest = Map.of(
                "title", title,
                "severity", severity,
                "priority", priority
        );
        mockMvc.perform(post("/api/v1/incidents")
                        .with(jwt().jwt(builder -> builder.subject(CREATOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }
}
