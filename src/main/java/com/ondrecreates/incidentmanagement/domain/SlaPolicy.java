package com.ondrecreates.incidentmanagement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "sla_policy")
public class SlaPolicy {

    @Id
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(name = "sla_minutes", nullable = false)
    private int slaMinutes;

    @Column(name = "near_breach_percentage", nullable = false)
    private int nearBreachPercentage;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SlaPolicy() {
        // JPA
    }

    public Duration slaDuration() {
        return Duration.ofMinutes(slaMinutes);
    }

    public Duration nearBreachDuration() {
        return Duration.ofMinutes((long) slaMinutes * nearBreachPercentage / 100);
    }

    public void update(int slaMinutes, int nearBreachPercentage) {
        this.slaMinutes = slaMinutes;
        this.nearBreachPercentage = nearBreachPercentage;
    }

    public Severity getSeverity() {
        return severity;
    }

    public int getSlaMinutes() {
        return slaMinutes;
    }

    public int getNearBreachPercentage() {
        return nearBreachPercentage;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
