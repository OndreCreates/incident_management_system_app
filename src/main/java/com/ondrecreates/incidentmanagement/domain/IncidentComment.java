package com.ondrecreates.incidentmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "incident_comment")
public class IncidentComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Column(name = "author_user_id", nullable = false)
    private String authorUserId;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean edited;

    @Column(nullable = false)
    private boolean deleted;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected IncidentComment() {
        // JPA
    }

    public IncidentComment(Incident incident, String authorUserId, String content) {
        this.incident = incident;
        this.authorUserId = authorUserId;
        this.content = content;
    }

    public void edit(String newContent) {
        this.content = newContent;
        this.edited = true;
    }

    /** Soft delete -- incident_timeline.comment_id keeps pointing at this row (see
     * V8__editable_comments.sql for why a hard delete isn't an option). */
    public void softDelete() {
        this.deleted = true;
    }

    public Long getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public String getAuthorUserId() {
        return authorUserId;
    }

    public String getContent() {
        return content;
    }

    public boolean isEdited() {
        return edited;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
