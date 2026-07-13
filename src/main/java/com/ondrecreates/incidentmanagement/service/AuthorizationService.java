package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Role;
import com.ondrecreates.incidentmanagement.exception.InsufficientRoleException;
import com.ondrecreates.incidentmanagement.repository.AppUserRoleRepository;
import org.springframework.stereotype.Service;

/**
 * Roles live in our own app_user_role table, not a JWT claim -- identity_server_app's
 * tokens carry no role information (see V9__user_roles.sql). Anyone with no row here is
 * MEMBER by default (least privilege): they get the full day-to-day incident workflow
 * (create/transition/comment/assign), just not the org-wide admin actions gated below.
 */
@Service
public class AuthorizationService {

    private final AppUserRoleRepository roleRepository;

    public AuthorizationService(AppUserRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role roleOf(String userEmail) {
        return roleRepository.findById(userEmail).map(r -> r.getRole()).orElse(Role.MEMBER);
    }

    public void requireAdmin(String userEmail) {
        if (roleOf(userEmail) != Role.ADMIN) {
            throw new InsufficientRoleException(userEmail);
        }
    }
}
