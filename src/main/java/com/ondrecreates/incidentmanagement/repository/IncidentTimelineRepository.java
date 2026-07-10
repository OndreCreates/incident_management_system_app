package com.ondrecreates.incidentmanagement.repository;

import com.ondrecreates.incidentmanagement.domain.IncidentTimelineEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentTimelineRepository extends JpaRepository<IncidentTimelineEntry, Long> {
}
