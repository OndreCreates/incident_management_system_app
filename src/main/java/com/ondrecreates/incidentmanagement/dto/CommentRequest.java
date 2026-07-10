package com.ondrecreates.incidentmanagement.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequest(
        @NotBlank String content
) {
}
