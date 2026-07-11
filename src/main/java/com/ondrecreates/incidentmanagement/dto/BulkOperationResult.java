package com.ondrecreates.incidentmanagement.dto;

/**
 * One incident's outcome within a bulk operation. Bulk endpoints are never
 * all-or-nothing -- a batch mixing valid and invalid transitions still
 * applies the valid ones, so each item reports its own success/error.
 */
public record BulkOperationResult(
        Long incidentId,
        boolean success,
        String error
) {

    public static BulkOperationResult success(Long incidentId) {
        return new BulkOperationResult(incidentId, true, null);
    }

    public static BulkOperationResult failure(Long incidentId, String error) {
        return new BulkOperationResult(incidentId, false, error);
    }
}
