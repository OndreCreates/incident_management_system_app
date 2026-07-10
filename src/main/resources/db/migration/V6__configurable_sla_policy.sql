-- Fáze 3: SLA politika přesouvá z hardcoded Severity enum hodnot do
-- admin-spravované tabulky. Seedované hodnoty jsou přesně to, co bylo
-- předtím napevno v kódu (Severity.java) -- jen teď editovatelné za běhu.
CREATE TABLE sla_policy (
    severity                ENUM ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW') PRIMARY KEY,
    sla_minutes             INT       NOT NULL,
    near_breach_percentage  INT       NOT NULL,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;

INSERT INTO sla_policy (severity, sla_minutes, near_breach_percentage)
VALUES ('CRITICAL', 240, 80),
       ('HIGH', 480, 80),
       ('MEDIUM', 1440, 80),
       ('LOW', 4320, 80);
