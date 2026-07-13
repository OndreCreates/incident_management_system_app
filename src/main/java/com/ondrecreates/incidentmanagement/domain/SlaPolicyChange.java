package com.ondrecreates.incidentmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Append-only audit record for SlaPolicy edits -- SlaPolicy itself only ever holds the
 * current values, so "who changed this and when" needs its own log, same principle as
 * IncidentTimelineEntry for incidents.
 */
@Entity
@Table(name = "sla_policy_change")
public class SlaPolicyChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Column(name = "old_sla_minutes", nullable = false)
    private int oldSlaMinutes;

    @Column(name = "old_near_breach_percentage", nullable = false)
    private int oldNearBreachPercentage;

    @Column(name = "new_sla_minutes", nullable = false)
    private int newSlaMinutes;

    @Column(name = "new_near_breach_percentage", nullable = false)
    private int newNearBreachPercentage;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;

    protected SlaPolicyChange() {
        // JPA
    }

    public SlaPolicyChange(Severity severity, int oldSlaMinutes, int oldNearBreachPercentage, int newSlaMinutes,
                            int newNearBreachPercentage, String changedBy) {
        this.severity = severity;
        this.oldSlaMinutes = oldSlaMinutes;
        this.oldNearBreachPercentage = oldNearBreachPercentage;
        this.newSlaMinutes = newSlaMinutes;
        this.newNearBreachPercentage = newNearBreachPercentage;
        this.changedBy = changedBy;
    }

    public Long getId() {
        return id;
    }

    public Severity getSeverity() {
        return severity;
    }

    public int getOldSlaMinutes() {
        return oldSlaMinutes;
    }

    public int getOldNearBreachPercentage() {
        return oldNearBreachPercentage;
    }

    public int getNewSlaMinutes() {
        return newSlaMinutes;
    }

    public int getNewNearBreachPercentage() {
        return newNearBreachPercentage;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}
