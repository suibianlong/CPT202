package com.cpt202.reviewworkflow.controller;

import com.cpt202.reviewworkflow.dto.ApiErrorResponse;
import com.cpt202.reviewworkflow.exception.ReviewNotFoundException;
import com.cpt202.reviewworkflow.exception.ReviewStatusConflictException;
import com.cpt202.reviewworkflow.exception.ReviewValidationException;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReviewControllerAdvice {

    @ExceptionHandler(ReviewValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(ReviewValidationException exception) {
        return ResponseEntity.badRequest()
            .body(new ApiErrorResponse("VALIDATION_ERROR", exception.getMessage(), exception.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        Map<String, String> details = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            details.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.badRequest()
            .body(new ApiErrorResponse("VALIDATION_ERROR", "Request validation failed.", details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, String> details = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation -> details.put(
            violation.getPropertyPath().toString(),
            violation.getMessage()
        ));

        return ResponseEntity.badRequest()
            .body(new ApiErrorResponse("VALIDATION_ERROR", "Request validation failed.", details));
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ReviewNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiErrorResponse("NOT_FOUND", exception.getMessage(), Map.of()));
    }

    @ExceptionHandler(ReviewStatusConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleStatusConflict(ReviewStatusConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiErrorResponse("STATUS_CONFLICT", exception.getMessage(), Map.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiErrorResponse("INTERNAL_ERROR", exception.getMessage(), Map.of()));
    }
}
