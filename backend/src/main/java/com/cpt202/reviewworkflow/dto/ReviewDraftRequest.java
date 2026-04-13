package com.cpt202.reviewworkflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewDraftRequest(
    @NotNull Long submissionId,
    @NotNull Long reviewerId,
    @NotBlank String feedbackComment
) {
}
