package com.example.cpt202heritage.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.util.StringUtils;

public record ReviewDecisionRequest(
        @NotNull
        @Positive
        Long submissionId,
        @NotNull
        @Positive
        Long resourceId,
        @NotNull
        @Positive
        Integer versionNo,
        @NotNull
        @Positive
        Long reviewerId,
        @NotNull
        ReviewAction action,
        String feedbackComment
) {
    @AssertTrue(message = "feedbackComment is required when action is REJECT")
    public boolean isRejectFeedbackValid() {
        return action != ReviewAction.REJECT || StringUtils.hasText(feedbackComment);
    }
}
