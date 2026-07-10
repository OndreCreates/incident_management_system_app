package com.ondrecreates.incidentmanagement.dto;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.IncidentTimelineEntry;
import java.util.List;

public record IncidentDetailResponse(
        IncidentResponse incident,
        List<TimelineEntryResponse> timeline
) {

    public static IncidentDetailResponse from(Incident incident, List<IncidentTimelineEntry> timeline) {
        return new IncidentDetailResponse(
                IncidentResponse.from(incident),
                timeline.stream().map(TimelineEntryResponse::from).toList()
        );
    }
}
