package com.example.cpt202heritage.dto;

import java.time.LocalDateTime;

public record RejectionDraftSection(
    String feedbackComment,
    LocalDateTime savedAt,
    Long savedByReviewerId,
    String savedByReviewerName
) {
}
