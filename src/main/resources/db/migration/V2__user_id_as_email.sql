-- identity_server_app dává do JWT sub claimu uživatelův email, ne numerické
-- AppUser.id (žádný jiný claim s numerickým ID token nenese). External user
-- reference proto přechází z BIGINT na VARCHAR(255) napříč všemi tabulkami.
ALTER TABLE incident MODIFY assigned_user_id VARCHAR(255);
ALTER TABLE incident MODIFY created_by VARCHAR(255) NOT NULL;
ALTER TABLE incident_timeline MODIFY actor_user_id VARCHAR(255) NOT NULL;
ALTER TABLE incident_comment MODIFY author_user_id VARCHAR(255) NOT NULL;
