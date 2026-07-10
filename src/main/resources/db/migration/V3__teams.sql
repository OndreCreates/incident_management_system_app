-- Fáze 2: přiřazení na tým, ne jen na jednotlivce. Tým a individuální assignee
-- jsou nezávislé -- incident se typicky nejdřív routuje na tým, pak si ho
-- konkrétní člověk z týmu vezme (assigned_user_id), stejně jako u ASSIGNMENT.
CREATE TABLE team (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_team_name UNIQUE (name)
) ENGINE = InnoDB;

CREATE TABLE team_member (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id    BIGINT       NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_member_team FOREIGN KEY (team_id) REFERENCES team (id),
    CONSTRAINT uq_team_member UNIQUE (team_id, user_email)
) ENGINE = InnoDB;

ALTER TABLE incident
    ADD COLUMN assigned_team_id BIGINT NULL,
    ADD CONSTRAINT fk_incident_team FOREIGN KEY (assigned_team_id) REFERENCES team (id);

CREATE INDEX idx_incident_assigned_team_id ON incident (assigned_team_id);

ALTER TABLE incident_timeline
    MODIFY event_type ENUM ('STATUS_CHANGE', 'ASSIGNMENT', 'TEAM_ASSIGNMENT', 'COMMENT') NOT NULL;
