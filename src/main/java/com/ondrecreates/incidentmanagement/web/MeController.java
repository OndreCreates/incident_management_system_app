package com.ondrecreates.incidentmanagement.web;

import com.ondrecreates.incidentmanagement.dto.MeResponse;
import com.ondrecreates.incidentmanagement.service.AuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Me", description = "Current user's identity and role")
@SecurityRequirement(name = "bearerAuth")
public class MeController {

    private final AuthorizationService authorizationService;

    public MeController(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @Operation(summary = "Get the current user's email and role", description = "Role comes from our own "
            + "app_user_role table (see AuthorizationService), not a JWT claim -- lets the admin panel "
            + "show/hide admin-only UI (bulk actions, SLA policy edits) without guessing from the token.")
    public MeResponse me(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getSubject();
        return new MeResponse(email, authorizationService.roleOf(email));
    }
}
