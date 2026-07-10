package com.ondrecreates.incidentmanagement.web;

import com.ondrecreates.incidentmanagement.domain.Severity;
import com.ondrecreates.incidentmanagement.dto.SlaPolicyResponse;
import com.ondrecreates.incidentmanagement.dto.UpdateSlaPolicyRequest;
import com.ondrecreates.incidentmanagement.service.SlaPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sla-policies")
@Tag(name = "SLA Policies", description = "Fáze 3 -- admin-configurable severity -> SLA duration mapping")
@SecurityRequirement(name = "bearerAuth")
public class SlaPolicyController {

    private final SlaPolicyService slaPolicyService;

    public SlaPolicyController(SlaPolicyService slaPolicyService) {
        this.slaPolicyService = slaPolicyService;
    }

    @GetMapping
    @Operation(summary = "List SLA policies", description = "One row per Severity, fixed set.")
    public List<SlaPolicyResponse> list() {
        return slaPolicyService.listPolicies().stream().map(SlaPolicyResponse::from).toList();
    }

    @PutMapping("/{severity}")
    @Operation(summary = "Update an SLA policy", description = "Only affects incidents created after the update -- "
            + "already-open incidents keep their originally computed deadline.")
    public SlaPolicyResponse update(@PathVariable Severity severity, @Valid @RequestBody UpdateSlaPolicyRequest request) {
        return SlaPolicyResponse.from(
                slaPolicyService.updatePolicy(severity, request.slaMinutes(), request.nearBreachPercentage()));
    }
}
