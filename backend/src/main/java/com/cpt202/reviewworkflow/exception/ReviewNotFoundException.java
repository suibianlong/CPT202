package com.cpt202.reviewworkflow.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(Long submissionId) {
        super("Review submission " + submissionId + " was not found.");
    }
}
