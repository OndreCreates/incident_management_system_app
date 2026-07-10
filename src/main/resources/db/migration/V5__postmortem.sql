-- Fáze 2: postmortem modul. Oddělené od incident.root_cause/resolution
-- (Fáze 1 pole vyplněná při samotném řešení) -- postmortem je strukturovaný
-- dokument napsaný až po uzavření, s dopadem a lessons learned navíc.
CREATE TABLE incident_postmortem (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id     BIGINT       NOT NULL,
    impact          TEXT         NOT NULL,
    root_cause      TEXT         NOT NULL,
    resolution      TEXT         NOT NULL,
    lessons_learned TEXT         NOT NULL,
    action_items    TEXT         NULL,
    author_user_id  VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_postmortem_incident FOREIGN KEY (incident_id) REFERENCES incident (id),
    CONSTRAINT uq_postmortem_incident UNIQUE (incident_id)
) ENGINE = InnoDB;
