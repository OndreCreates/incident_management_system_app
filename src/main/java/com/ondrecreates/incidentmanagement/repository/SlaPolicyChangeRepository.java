package com.ondrecreates.incidentmanagement.repository;

import com.ondrecreates.incidentmanagement.domain.SlaPolicyChange;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaPolicyChangeRepository extends JpaRepository<SlaPolicyChange, Long> {

    List<SlaPolicyChange> findAllByOrderByChangedAtDesc();
}
