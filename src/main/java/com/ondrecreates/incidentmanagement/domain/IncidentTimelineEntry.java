package com.ondrecreates.incidentmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "incident_timeline")
public class IncidentTimelineEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id", nullable = false)
    private Incident incident;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private Status fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status")
    private Status toStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    private IncidentComment comment;

    @Column(name = "actor_user_id", nullable = false)
    private String actorUserId;

    private String note;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected IncidentTimelineEntry() {
        // JPA
    }

    private IncidentTimelineEntry(Incident incident, EventType eventType, Status fromStatus,
                                   Status toStatus, IncidentComment comment, String actorUserId, String note) {
        this.incident = incident;
        this.eventType = eventType;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.comment = comment;
        this.actorUserId = actorUserId;
        this.note = note;
    }

    public static IncidentTimelineEntry forStatusChange(Incident incident, Status fromStatus, Status toStatus,
                                                          String actorUserId, String note) {
        return new IncidentTimelineEntry(incident, EventType.STATUS_CHANGE, fromStatus, toStatus, null,
                actorUserId, note);
    }

    public static IncidentTimelineEntry forAssignment(Incident incident, String actorUserId, String note) {
        return new IncidentTimelineEntry(incident, EventType.ASSIGNMENT, null, null, null, actorUserId, note);
    }

    public static IncidentTimelineEntry forTeamAssignment(Incident incident, String actorUserId, String note) {
        return new IncidentTimelineEntry(incident, EventType.TEAM_ASSIGNMENT, null, null, null, actorUserId, note);
    }

    public static IncidentTimelineEntry forComment(Incident incident, IncidentComment comment, String actorUserId) {
        return new IncidentTimelineEntry(incident, EventType.COMMENT, null, null, comment, actorUserId, null);
    }

    public Long getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Status getFromStatus() {
        return fromStatus;
    }

    public Status getToStatus() {
        return toStatus;
    }

    public IncidentComment getComment() {
        return comment;
    }

    public String getActorUserId() {
        return actorUserId;
    }

    public String getNote() {
        return note;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
