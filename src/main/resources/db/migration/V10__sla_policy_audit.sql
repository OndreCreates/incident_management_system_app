-- Append-only, same principle as incident_timeline -- SlaPolicy itself only ever holds
-- the current values (see SlaPolicyService), so "who changed this and when" needs its
-- own log rather than overwriting a single row's history away.
CREATE TABLE sla_policy_change (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    severity                    VARCHAR(20)  NOT NULL,
    old_sla_minutes             INT          NOT NULL,
    old_near_breach_percentage  INT          NOT NULL,
    new_sla_minutes             INT          NOT NULL,
    new_near_breach_percentage  INT          NOT NULL,
    changed_by                  VARCHAR(255) NOT NULL,
    changed_at                  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sla_policy_change_changed_at ON sla_policy_change (changed_at);
