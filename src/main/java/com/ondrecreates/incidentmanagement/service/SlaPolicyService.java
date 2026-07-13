package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.SlaPolicy;
import com.ondrecreates.incidentmanagement.domain.SlaPolicyChange;
import com.ondrecreates.incidentmanagement.repository.SlaPolicyChangeRepository;
import com.ondrecreates.incidentmanagement.repository.SlaPolicyRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * One row per Severity value, seeded by V6__configurable_sla_policy.sql --
 * fixed set, never created/deleted via the API, only edited. A missing row
 * is a data integrity bug, not a normal 404 case (hence IllegalStateException,
 * not a domain NotFoundException).
 */
@Service
public class SlaPolicyService {

    private final SlaPolicyRepository slaPolicyRepository;
    private final SlaPolicyChangeRepository slaPolicyChangeRepository;
    private final AuthorizationService authorizationService;

    public SlaPolicyService(SlaPolicyRepository slaPolicyRepository,
                             SlaPolicyChangeRepository slaPolicyChangeRepository,
                             AuthorizationService authorizationService) {
        this.slaPolicyRepository = slaPolicyRepository;
        this.slaPolicyChangeRepository = slaPolicyChangeRepository;
        this.authorizationService = authorizationService;
    }

    public SlaPolicy getPolicy(Severity severity) {
        return slaPolicyRepository.findById(severity)
                .orElseThrow(() -> new IllegalStateException("No SLA policy seeded for severity " + severity));
    }

    public List<SlaPolicy> listPolicies() {
        return slaPolicyRepository.findAll();
    }

    public List<SlaPolicyChange> listChanges() {
        return slaPolicyChangeRepository.findAllByOrderByChangedAtDesc();
    }

    // Org-wide setting, not per-incident -- ADMIN only, unlike the day-to-day incident
    // workflow (create/transition/comment) which any authenticated MEMBER can do.
    @Transactional
    public SlaPolicy updatePolicy(Severity severity, int slaMinutes, int nearBreachPercentage, String actorUserId) {
        authorizationService.requireAdmin(actorUserId);
        SlaPolicy policy = getPolicy(severity);

        // Own append-only log, not derived from updated_at -- SlaPolicy only ever holds
        // the CURRENT values, so recording who/when/from-what-to-what needs its own row.
        slaPolicyChangeRepository.save(new SlaPolicyChange(severity, policy.getSlaMinutes(),
                policy.getNearBreachPercentage(), slaMinutes, nearBreachPercentage, actorUserId));

        policy.update(slaMinutes, nearBreachPercentage);
        return slaPolicyRepository.save(policy);
    }
}
