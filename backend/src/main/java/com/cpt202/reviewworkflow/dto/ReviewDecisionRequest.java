package com.cpt202.reviewworkflow.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewDecisionRequest(
    @NotNull Long submissionId,
    @NotNull Long resourceId,
    @NotNull Integer versionNo,
    @NotNull Long reviewerId,
    @NotNull ReviewAction action,
    String feedbackComment
) {
}
