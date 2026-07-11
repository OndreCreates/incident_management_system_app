package com.ondrecreates.incidentmanagement.service;

import com.ondrecreates.incidentmanagement.domain.Status;
import com.ondrecreates.incidentmanagement.dto.BulkOperationResult;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Kept as a bean separate from IncidentService so each item's call to
 * incidentService.transition()/assignUser() goes through the real Spring
 * proxy and gets its own transaction. Bulk operations are deliberately
 * per-item, not all-or-nothing: one incident rejecting the transition
 * must not roll back the others that succeeded (self-invocation from
 * inside IncidentService would share one transaction and defeat that).
 */
@Service
public class BulkOperationService {

    private final IncidentService incidentService;

    public BulkOperationService(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    public List<BulkOperationResult> bulkTransition(List<Long> incidentIds, Status targetStatus, String note,
                                                      String actorUserId) {
        return incidentIds.stream()
                .map(id -> attempt(id, () -> incidentService.transition(id, targetStatus, null, actorUserId, note)))
                .toList();
    }

    public List<BulkOperationResult> bulkAssign(List<Long> incidentIds, String assignedUserId, String actorUserId) {
        return incidentIds.stream()
                .map(id -> attempt(id, () -> incidentService.assignUser(id, assignedUserId, actorUserId)))
                .toList();
    }

    private BulkOperationResult attempt(Long incidentId, Runnable action) {
        try {
            action.run();
            return BulkOperationResult.success(incidentId);
        } catch (RuntimeException ex) {
            return BulkOperationResult.failure(incidentId, ex.getMessage());
        }
    }
}
