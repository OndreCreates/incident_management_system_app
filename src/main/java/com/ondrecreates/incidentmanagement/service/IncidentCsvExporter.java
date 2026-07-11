package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Incident;
import java.util.List;
import org.springframework.stereotype.Component;

/** Kept separate from IncidentService -- CSV formatting is a presentation concern, not a domain one. */
@Component
public class IncidentCsvExporter {

    private static final String[] HEADER = {
            "id", "title", "status", "severity", "priority", "assignedUserId", "assignedTeamId",
            "slaDeadline", "slaBreached", "createdBy", "createdAt", "updatedAt",
    };

    public String toCsv(List<Incident> incidents) {
        StringBuilder csv = new StringBuilder();
        writeRow(csv, HEADER);
        for (Incident incident : incidents) {
            writeRow(csv,
                    String.valueOf(incident.getId()),
                    incident.getTitle(),
                    incident.getStatus().name(),
                    incident.getSeverity().name(),
                    incident.getPriority().name(),
                    incident.getAssignedUserId(),
                    incident.getAssignedTeam() != null ? String.valueOf(incident.getAssignedTeam().getId()) : "",
                    String.valueOf(incident.getSlaDeadline()),
                    String.valueOf(incident.isSlaBreached()),
                    incident.getCreatedBy(),
                    String.valueOf(incident.getCreatedAt()),
                    String.valueOf(incident.getUpdatedAt()));
        }
        return csv.toString();
    }

    private void writeRow(StringBuilder csv, String... fields) {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                csv.append(',');
            }
            csv.append(escape(fields[i]));
        }
        csv.append("\r\n");
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        boolean needsQuoting = value.contains(",") || value.contains("\"") || value.contains("\n")
                || value.contains("\r");
        String escaped = value.replace("\"", "\"\"");
        return needsQuoting ? "\"" + escaped + "\"" : escaped;
    }
}
