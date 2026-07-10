package com.ondrecreates.incidentmanagement.web;

import com.ondrecreates.incidentmanagement.dto.AddTeamMemberRequest;
import com.ondrecreates.incidentmanagement.dto.CreateTeamRequest;
import com.ondrecreates.incidentmanagement.dto.TeamResponse;
import com.ondrecreates.incidentmanagement.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/teams")
@Tag(name = "Teams", description = "Team assignment (Fáze 2) -- incidents route to a team, then an individual takes ownership")
@SecurityRequirement(name = "bearerAuth")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a team", description = "Requires at least one member.")
    public TeamResponse create(@Valid @RequestBody CreateTeamRequest request) {
        return TeamResponse.from(teamService.createTeam(request.name(), request.memberEmails()));
    }

    @GetMapping
    @Operation(summary = "List teams")
    public List<TeamResponse> list() {
        return teamService.listTeams().stream().map(TeamResponse::from).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get team detail")
    public TeamResponse detail(@PathVariable Long id) {
        return TeamResponse.from(teamService.getTeamOrThrow(id));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add a member to a team")
    public TeamResponse addMember(@PathVariable Long id, @Valid @RequestBody AddTeamMemberRequest request) {
        return TeamResponse.from(teamService.addMember(id, request.userEmail()));
    }
}
