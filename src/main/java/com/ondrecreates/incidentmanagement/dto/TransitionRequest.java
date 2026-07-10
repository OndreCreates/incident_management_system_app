package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.Status;
import jakarta.validation.constraints.NotNull;

/**
 * assignedUserId je volitelný — vyplní se jen když přechod zároveň
 * přiřazuje incident jinému uživateli (typicky CREATED -> ASSIGNED,
 * nebo reassignment INVESTIGATING -> ASSIGNED). Zapíše se jako
 * samostatná ASSIGNMENT timeline entry vedle STATUS_CHANGE entry.
 *
 * actorUserId v těle není — bere se z JWT sub claimu (email), nikdy
 * z požadavku samotného.
 */
public record TransitionRequest(
        @NotNull Status targetStatus,
        String note,
        String assignedUserId
) {
}
