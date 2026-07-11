package com.ondrecreates.incidentmanagement.notification;

/**
 * Talks to notification_center_app's REST API (POST /api/v1/notifications,
 * X-API-Key auth -- verified against the real running service, not guessed;
 * see incident_management_app's README for how the contract was confirmed).
 *
 * Interface (not just a concrete @Component) so tests can substitute a plain
 * in-memory fake instead of mocking -- avoids fighting Mockito's inline mock
 * maker, which fails under some JDKs (see RestNotificationClient's test double).
 */
public interface NotificationClient {

    void sendEmail(String recipient, String subject, String body);

    /** Live push to the admin panel via notification_center_app's WEBSOCKET channel --
     * best-effort, same posture as sendEmail: never fatal if the recipient isn't connected. */
    void sendWebSocket(String recipient, String subject, String body);
}
