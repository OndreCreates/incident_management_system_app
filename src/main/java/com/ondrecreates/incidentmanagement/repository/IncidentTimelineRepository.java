package com.ondrecreates.incidentmanagement.repository;

import com.ondrecreates.incidentmanagement.domain.Incident;
import com.ondrecreates.incidentmanagement.domain.IncidentTimelineEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IncidentTimelineRepository extends JpaRepository<IncidentTimelineEntry, Long> {

    // LEFT JOIN FETCH comment: open-in-view is disabled, so entry.getComment().getContent()
    // (used by TimelineEntryResponse to show comment text in the timeline) would otherwise
    // throw LazyInitializationException once the session backing a derived-query result closes.
    @Query("SELECT t FROM IncidentTimelineEntry t LEFT JOIN FETCH t.comment WHERE t.incident = :incident "
            + "ORDER BY t.createdAt ASC")
    List<IncidentTimelineEntry> findByIncidentOrderByCreatedAtAsc(@Param("incident") Incident incident);
}
