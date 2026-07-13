package com.ondrecreates.incidentmanagement.repository;

import com.ondrecreates.incidentmanagement.domain.AppUserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRoleRepository extends JpaRepository<AppUserRole, String> {
}
