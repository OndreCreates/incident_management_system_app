package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.domain.SlaPolicy;
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

    public SlaPolicyService(SlaPolicyRepository slaPolicyRepository) {
        this.slaPolicyRepository = slaPolicyRepository;
    }

    public SlaPolicy getPolicy(Severity severity) {
        return slaPolicyRepository.findById(severity)
                .orElseThrow(() -> new IllegalStateException("No SLA policy seeded for severity " + severity));
    }

    public List<SlaPolicy> listPolicies() {
        return slaPolicyRepository.findAll();
    }

    @Transactional
    public SlaPolicy updatePolicy(Severity severity, int slaMinutes, int nearBreachPercentage) {
        SlaPolicy policy = getPolicy(severity);
        policy.update(slaMinutes, nearBreachPercentage);
        return slaPolicyRepository.save(policy);
    }
}
