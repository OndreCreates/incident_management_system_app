package com.ondrecreates.incidentmanagement.repository;

import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.SlaPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Severity> {
}
