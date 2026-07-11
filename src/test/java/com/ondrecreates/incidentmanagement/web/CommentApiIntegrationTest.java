package com.ondrecreates.incidentmanagement.web;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
 * Quick-win acceptance: comments are editable/deletable, but only by their
 * original author, and a delete is a soft delete -- the timeline entry stays
 * (append-only), just with its content hidden.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommentApiIntegrationTest {

    private static final String AUTHOR_EMAIL = "author@example.com";
    private static final String OTHER_EMAIL = "other@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authorCanEditOwnComment() throws Exception {
        Long incidentId = createIncident();
        Long commentId = addComment(incidentId, AUTHOR_EMAIL, "Original text");

        mockMvc.perform(put("/api/v1/incidents/{id}/comments/{commentId}", incidentId, commentId)
                        .with(jwt().jwt(builder -> builder.subject(AUTHOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "Corrected text"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Corrected text"))
                .andExpect(jsonPath("$.edited").value(true));
    }

    @Test
    void nonAuthorCannotEditComment() throws Exception {
        Long incidentId = createIncident();
        Long commentId = addComment(incidentId, AUTHOR_EMAIL, "Original text");

        mockMvc.perform(put("/api/v1/incidents/{id}/comments/{commentId}", incidentId, commentId)
                        .with(jwt().jwt(builder -> builder.subject(OTHER_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "Hijacked text"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("COMMENT_AUTHOR_MISMATCH"));
    }

    @Test
    void authorCanDeleteOwnCommentAndTimelineHidesContentButKeepsEntry() throws Exception {
        Long incidentId = createIncident();
        Long commentId = addComment(incidentId, AUTHOR_EMAIL, "Oops, wrong incident");

        mockMvc.perform(delete("/api/v1/incidents/{id}/comments/{commentId}", incidentId, commentId)
                        .with(jwt().jwt(builder -> builder.subject(AUTHOR_EMAIL))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/incidents/{id}/timeline", incidentId).with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventType").value("COMMENT"))
                .andExpect(jsonPath("$[0].commentId").value(commentId))
                .andExpect(jsonPath("$[0].commentDeleted").value(true))
                .andExpect(jsonPath("$[0].commentContent").doesNotExist());
    }

    @Test
    void nonAuthorCannotDeleteComment() throws Exception {
        Long incidentId = createIncident();
        Long commentId = addComment(incidentId, AUTHOR_EMAIL, "Original text");

        mockMvc.perform(delete("/api/v1/incidents/{id}/comments/{commentId}", incidentId, commentId)
                        .with(jwt().jwt(builder -> builder.subject(OTHER_EMAIL))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("COMMENT_AUTHOR_MISMATCH"));
    }

    private Long createIncident() throws Exception {
        Map<String, Object> createRequest = Map.of(
                "title", "Something needs comments",
                "severity", "LOW",
                "priority", "P4"
        );
        String response = mockMvc.perform(post("/api/v1/incidents")
                        .with(jwt().jwt(builder -> builder.subject(AUTHOR_EMAIL)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long addComment(Long incidentId, String author, String content) throws Exception {
        String response = mockMvc.perform(post("/api/v1/incidents/{id}/comments", incidentId)
                        .with(jwt().jwt(builder -> builder.subject(author)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", content))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
