package com.ondrecreates.incidentmanagement.repository;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.IncidentTimelineEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentTimelineRepository extends JpaRepository<IncidentTimelineEntry, Long> {

    List<IncidentTimelineEntry> findByIncidentOrderByCreatedAtAsc(Incident incident);
}
