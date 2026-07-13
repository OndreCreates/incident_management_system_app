package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Incident;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Kept separate from IncidentService -- CSV formatting is a presentation concern, not a
 * domain one. Writes straight to the response's OutputStream row by row instead of
 * building the whole document as a String first -- that extra copy (and the byte[] copy
 * after it) is pure overhead once the caller's only going to write it out anyway.
 */
@Component
public class IncidentCsvExporter {

    private static final String[] HEADER = {
            "id", "title", "status", "severity", "priority", "assignedUserId", "assignedTeamId",
            "slaDeadline", "slaBreached", "createdBy", "createdAt", "updatedAt",
    };

    public void writeCsv(List<Incident> incidents, OutputStream out) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writeRow(writer, HEADER);
            for (Incident incident : incidents) {
                writeRow(writer,
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
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeRow(BufferedWriter writer, String... fields) throws IOException {
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                writer.write(',');
            }
            writer.write(escape(fields[i]));
        }
        writer.write("\r\n");
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
