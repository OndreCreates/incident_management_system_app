-- Fáze 2: eskalace. near_breach_at je počítané při vytvoření (80 % SLA okna,
-- stejně jako sla_deadline), ne odvozené za běhu -- konzistentní s tím, jak
-- se sla_deadline počítá jednou při vytvoření podle severity.
ALTER TABLE incident
    ADD COLUMN near_breach_at TIMESTAMP NULL;

UPDATE incident
SET near_breach_at = sla_deadline
WHERE near_breach_at IS NULL;

ALTER TABLE incident
    MODIFY near_breach_at TIMESTAMP NOT NULL;

ALTER TABLE incident
    ADD COLUMN near_breach_notified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN breach_notified       BOOLEAN NOT NULL DEFAULT FALSE;
