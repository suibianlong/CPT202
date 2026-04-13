package com.cpt202.reviewworkflow.dto;

import java.time.LocalDateTime;

public record ReviewDraftResponse(
    Long submissionId,
    String feedbackComment,
    LocalDateTime savedAt,
    Long savedByReviewerId,
    String savedByReviewerName
) {
}
