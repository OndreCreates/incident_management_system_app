package com.ondrecreates.incidentmanagement.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Best-effort by design: a flaky/unreachable notification service must never
 * break the escalation job or take down the incident that triggered it --
 * same posture as demo-client's revokeToken call in identity_server_app.
 */
@Component
public class RestNotificationClient implements NotificationClient {

    private static final Logger log = LoggerFactory.getLogger(RestNotificationClient.class);

    private final RestClient restClient;
    private final boolean configured;

    public RestNotificationClient(@Value("${notification.api-url}") String apiUrl,
                                   @Value("${notification.api-key}") String apiKey) {
        this.configured = !apiKey.isBlank();
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("X-API-Key", apiKey)
                .build();
    }

    @Override
    public void sendEmail(String recipient, String subject, String body) {
        if (!configured) {
            log.warn("notification.api-key not configured -- skipping escalation email to {}", recipient);
            return;
        }
        try {
            restClient.post()
                    .uri("/api/v1/notifications")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new NotificationRequest("EMAIL", recipient, subject, body))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.warn("Failed to send escalation email to {}: {}", recipient, ex.getMessage());
        }
    }

    private record NotificationRequest(String channel, String recipient, String subject, String body) {
    }
}
