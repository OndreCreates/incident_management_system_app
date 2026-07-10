package com.ondrecreates.incidentmanagement.dto;

public record DailyCountResponse(
        String date,
        long count
) {
}
