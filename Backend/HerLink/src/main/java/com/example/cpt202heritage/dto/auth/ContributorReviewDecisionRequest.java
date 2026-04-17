package com.example.cpt202heritage.dto.auth;

public class ContributorReviewDecisionRequest {

    private String decision;
    private String reviewComment;

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }
}
