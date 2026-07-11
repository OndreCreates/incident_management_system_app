-- Comments become editable/deletable by their author. Soft-delete, not
-- DELETE FROM: incident_timeline.comment_id references incident_comment,
-- and the timeline is append-only -- a hard delete would either violate
-- that FK or force deleting/rewriting a timeline row. `deleted` lets the
-- timeline keep pointing at the same row while the UI shows "[smazáno]".
ALTER TABLE incident_comment
    ADD COLUMN edited    BOOLEAN   NOT NULL DEFAULT FALSE,
    ADD COLUMN deleted   BOOLEAN   NOT NULL DEFAULT FALSE,
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
