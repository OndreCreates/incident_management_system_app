CREATE TABLE incident (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    title             VARCHAR(255)                                                        NOT NULL,
    description       TEXT,
    severity          ENUM ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW')                          NOT NULL,
    priority          ENUM ('P1', 'P2', 'P3', 'P4')                                       NOT NULL,
    status            ENUM ('CREATED', 'ASSIGNED', 'INVESTIGATING', 'MITIGATED', 'RESOLVED', 'CLOSED') NOT NULL,
    assigned_user_id  BIGINT,
    sla_deadline      TIMESTAMP                                                           NOT NULL,
    sla_breached      BOOLEAN                                                             NOT NULL DEFAULT FALSE,
    root_cause        TEXT,
    resolution        TEXT,
    created_by        BIGINT                                                              NOT NULL,
    created_at        TIMESTAMP                                                           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP                                                           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;

CREATE INDEX idx_incident_status ON incident (status);
CREATE INDEX idx_incident_severity ON incident (severity);
CREATE INDEX idx_incident_sla_breached ON incident (sla_breached);

CREATE TABLE incident_comment (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id      BIGINT                                       NOT NULL,
    author_user_id   BIGINT                                       NOT NULL,
    content          TEXT                                         NOT NULL,
    created_at       TIMESTAMP                                    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_incident_comment_incident
        FOREIGN KEY (incident_id) REFERENCES incident (id)
) ENGINE = InnoDB;

CREATE INDEX idx_incident_comment_incident_id ON incident_comment (incident_id);

-- comment_id doplňuje datový model ze zadání: popis říká, že COMMENT
-- záznam v timeline odkazuje na komentář přes ID místo duplikace textu,
-- tabulkový výčet sloupců to ale neuváděl — sloupec je nutný, aby ten záměr šel implementovat.
CREATE TABLE incident_timeline (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id      BIGINT                                                    NOT NULL,
    event_type       ENUM ('STATUS_CHANGE', 'ASSIGNMENT', 'COMMENT')           NOT NULL,
    from_status      ENUM ('CREATED', 'ASSIGNED', 'INVESTIGATING', 'MITIGATED', 'RESOLVED', 'CLOSED'),
    to_status        ENUM ('CREATED', 'ASSIGNED', 'INVESTIGATING', 'MITIGATED', 'RESOLVED', 'CLOSED'),
    comment_id       BIGINT,
    actor_user_id    BIGINT                                                    NOT NULL,
    note             TEXT,
    created_at       TIMESTAMP                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_incident_timeline_incident
        FOREIGN KEY (incident_id) REFERENCES incident (id),
    CONSTRAINT fk_incident_timeline_comment
        FOREIGN KEY (comment_id) REFERENCES incident_comment (id)
) ENGINE = InnoDB;

CREATE INDEX idx_incident_timeline_incident_id ON incident_timeline (incident_id);
