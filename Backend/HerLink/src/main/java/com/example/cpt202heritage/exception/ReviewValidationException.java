package com.example.cpt202heritage.exception;

import java.util.Map;

public class ReviewValidationException extends RuntimeException {
    private final Map<String, String> details;

    public ReviewValidationException(String message, Map<String, String> details) {
        super(message);
        this.details = Map.copyOf(details);
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
