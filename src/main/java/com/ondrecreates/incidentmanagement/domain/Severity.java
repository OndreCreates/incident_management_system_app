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
}
