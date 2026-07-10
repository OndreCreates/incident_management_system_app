package com.ondrecreates.incidentmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "incident_postmortem")
public class IncidentPostmortem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false, unique = true)
    private Long incidentId;

    @Column(nullable = false)
    private String impact;

    @Column(name = "root_cause", nullable = false)
    private String rootCause;

    @Column(nullable = false)
    private String resolution;

    @Column(name = "lessons_learned", nullable = false)
    private String lessonsLearned;

    @Column(name = "action_items")
    private String actionItems;

    @Column(name = "author_user_id", nullable = false)
    private String authorUserId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected IncidentPostmortem() {
        // JPA
    }

    public IncidentPostmortem(Long incidentId, String impact, String rootCause, String resolution,
                               String lessonsLearned, String actionItems, String authorUserId) {
        this.incidentId = incidentId;
        this.impact = impact;
        this.rootCause = rootCause;
        this.resolution = resolution;
        this.lessonsLearned = lessonsLearned;
        this.actionItems = actionItems;
        this.authorUserId = authorUserId;
    }

    public void update(String impact, String rootCause, String resolution, String lessonsLearned,
                        String actionItems) {
        this.impact = impact;
        this.rootCause = rootCause;
        this.resolution = resolution;
        this.lessonsLearned = lessonsLearned;
        this.actionItems = actionItems;
    }

    public Long getId() {
        return id;
    }

    public Long getIncidentId() {
        return incidentId;
    }

    public String getImpact() {
        return impact;
    }

    public String getRootCause() {
        return rootCause;
    }

    public String getResolution() {
        return resolution;
    }

    public String getLessonsLearned() {
        return lessonsLearned;
    }

    public String getActionItems() {
        return actionItems;
    }

    public String getAuthorUserId() {
        return authorUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
