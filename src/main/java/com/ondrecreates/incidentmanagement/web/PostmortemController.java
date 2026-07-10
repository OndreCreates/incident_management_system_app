package com.ondrecreates.incidentmanagement.web;

import com.ondrecreates.incidentmanagement.dto.PostmortemRequest;
import com.ondrecreates.incidentmanagement.dto.PostmortemResponse;
import com.ondrecreates.incidentmanagement.service.PostmortemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/incidents/{incidentId}/postmortem")
@Tag(name = "Postmortem", description = "Fáze 2 -- only for incidents already Resolved/Closed")
@SecurityRequirement(name = "bearerAuth")
public class PostmortemController {

    private final PostmortemService postmortemService;

    public PostmortemController(PostmortemService postmortemService) {
        this.postmortemService = postmortemService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a postmortem", description = "409 if the incident isn't Resolved/Closed yet, "
            + "or if one already exists.")
    @ApiResponse(responseCode = "409", description = "Incident not terminal, or postmortem already exists")
    public PostmortemResponse create(@PathVariable Long incidentId, @Valid @RequestBody PostmortemRequest request,
                                      @AuthenticationPrincipal Jwt jwt) {
        return PostmortemResponse.from(postmortemService.create(incidentId, request, jwt.getSubject()));
    }

    @GetMapping
    @Operation(summary = "Get the postmortem")
    public PostmortemResponse get(@PathVariable Long incidentId) {
        return PostmortemResponse.from(postmortemService.getOrThrow(incidentId));
    }

    @PutMapping
    @Operation(summary = "Update the postmortem")
    public PostmortemResponse update(@PathVariable Long incidentId, @Valid @RequestBody PostmortemRequest request) {
        return PostmortemResponse.from(postmortemService.update(incidentId, request));
    }
}
