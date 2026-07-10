-- Fáze 3: dashboard analytika (průměrná doba řešení) potřebuje vědět, kdy
-- byl incident naposledy vyřešen -- ne odvozovat to z timeline dotazem
-- při každém načtení dashboardu.
ALTER TABLE incident
    ADD COLUMN resolved_at TIMESTAMP NULL;
