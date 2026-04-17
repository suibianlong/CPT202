package com.example.cpt202heritage.dto;

import java.time.LocalDateTime;

public record ReviewDraftResponse(
    Long submissionId,
    String feedbackComment,
    LocalDateTime savedAt,
    Long savedByReviewerId,
    String savedByReviewerName
) {
}
