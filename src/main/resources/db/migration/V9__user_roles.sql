-- Local role table, not a JWT claim -- identity_server_app's tokens carry no role
-- information today, and adding one there is a change to a different portfolio project.
-- Defaults to MEMBER for anyone not listed here (least privilege); ADMIN is opt-in per row.
CREATE TABLE app_user_role (
    user_email VARCHAR(255) NOT NULL PRIMARY KEY,
    role       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed account used throughout the README's verification steps -- gets ADMIN so the
-- documented walkthrough can actually exercise the admin-only endpoints.
INSERT INTO app_user_role (user_email, role) VALUES ('admin@identity-server.dev', 'ADMIN');
