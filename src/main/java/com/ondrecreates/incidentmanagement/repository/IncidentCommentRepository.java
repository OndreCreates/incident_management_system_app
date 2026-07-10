package com.ondrecreates.incidentmanagement.repository;

import com.ondrecreates.incidentmanagement.domain.IncidentComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentCommentRepository extends JpaRepository<IncidentComment, Long> {
}
