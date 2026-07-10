package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.Status;
import jakarta.validation.constraints.NotNull;

/**
 * assignedUserId je volitelný — vyplní se jen když přechod zároveň
 * přiřazuje incident jinému uživateli (typicky CREATED -> ASSIGNED,
 * nebo reassignment INVESTIGATING -> ASSIGNED). Zapíše se jako
 * samostatná ASSIGNMENT timeline entry vedle STATUS_CHANGE entry.
 *
 * actorUserId je dočasně součástí těla requestu — Fáze 1D ho nahradí
 * hodnotou z JWT a toto pole zmizí z veřejného kontraktu.
 */
public record TransitionRequest(
        @NotNull Status targetStatus,
        String note,
        Long assignedUserId,
        @NotNull Long actorUserId
) {
}
