package com.cpt202.review.dto;

import java.time.LocalDateTime;

public record SubmissionSection(
        LocalDateTime submittedAt,
        Long submittedBy,
        String submissionNote,
        ResourceReviewStatus statusSnapshot
) {
}
