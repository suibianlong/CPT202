package com.cpt202.reviewworkflow.dto;

import java.util.Map;

public record ApiErrorResponse(
    String code,
    String message,
    Map<String, String> details
) {
}
