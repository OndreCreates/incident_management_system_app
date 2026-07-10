package com.ondrecreates.incidentmanagement.domain;

import java.time.Duration;

public enum Severity {
    CRITICAL(Duration.ofHours(4)),
    HIGH(Duration.ofHours(8)),
    MEDIUM(Duration.ofHours(24)),
    LOW(Duration.ofHours(72));

    private final Duration slaDuration;

    Severity(Duration slaDuration) {
        this.slaDuration = slaDuration;
    }

    public Duration getSlaDuration() {
        return slaDuration;
    }

    /** 80% of the SLA window -- when the near-breach escalation fires (Fáze 2). */
    public Duration getNearBreachDuration() {
        return slaDuration.multipliedBy(80).dividedBy(100);
    }
}
