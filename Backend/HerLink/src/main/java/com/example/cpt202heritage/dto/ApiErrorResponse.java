package com.example.cpt202heritage.dto;

import java.util.Map;

public record ApiErrorResponse(
    String code,
    String message,
    Map<String, String> details
) {
}
