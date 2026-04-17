package com.example.cpt202heritage.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(Long submissionId) {
        super("Review submission " + submissionId + " was not found.");
    }
}
