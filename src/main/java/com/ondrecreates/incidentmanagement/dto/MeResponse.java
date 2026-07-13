package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.Role;

public record MeResponse(String email, Role role) {
}
