package com.example.cpt202heritage.dto;

import java.time.LocalDateTime;

public record SubmissionSection(
        LocalDateTime submittedAt,
        Long submittedBy,
        String submissionNote,
        ResourceReviewStatus statusSnapshot
) {
}
