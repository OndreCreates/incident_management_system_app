package com.ondrecreates.incidentmanagement.web;

import com.ondrecreates.incidentmanagement.dto.DashboardAnalyticsResponse;
import com.ondrecreates.incidentmanagement.dto.DashboardSummaryResponse;
import com.ondrecreates.incidentmanagement.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Operational summary counts and Fáze 3 analytics")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get summary counts", description = "activeCount (not Resolved/Closed), "
            + "criticalCount (active + CRITICAL severity), breachedCount (slaBreached = true).")
    public DashboardSummaryResponse summary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get trend analytics", description = "avgResolutionMinutes (across all ever-resolved "
            + "incidents), slaComplianceRate (% of terminal incidents that were never breached), createdPerDay "
            + "(last 14 days).")
    public DashboardAnalyticsResponse analytics() {
        return dashboardService.getAnalytics();
    }
}
