package com.schedulingservice.api.infrastructure.web.exception;

import com.schedulingservice.api.domain.exception.DomainException;
import com.schedulingservice.api.domain.exception.ConflictException;
import com.schedulingservice.api.domain.exception.EntityNotFoundException;
import com.schedulingservice.api.domain.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String PROBLEM_BASE_URL = "https://api.schedulingservice.com/problems/";

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(final EntityNotFoundException ex, final WebRequest request) {
        logger.warn("Entity not found: {}", ex.getMessage());
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_BASE_URL + "entity-not-found"));
        problemDetail.setTitle("Entity Not Found");
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("entityType", ex.getEntityType());
        problemDetail.setProperty("entityId", ex.getEntityId());
        return problemDetail;
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidationException(final ValidationException ex, final WebRequest request) {
        logger.warn("Validation error: {}", ex.getMessage());
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_BASE_URL + "validation-error"));
        problemDetail.setTitle("Validation Error");
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("field", ex.getField());
        problemDetail.setProperty("rejectedValue", ex.getRejectedValue());
        return problemDetail;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflictException(final ConflictException ex, final WebRequest request) {
        logger.warn("Conflict: {}", ex.getMessage());
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_BASE_URL + "conflict"));
        problemDetail.setTitle("Conflict");
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("resource", ex.getResource());
        problemDetail.setProperty("reason", ex.getReason());
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final WebRequest request) {
        logger.warn("Method argument validation failed: {}", ex.getMessage());
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed for request body");
        problemDetail.setType(URI.create(PROBLEM_BASE_URL + "invalid-request"));
        problemDetail.setTitle("Invalid Request");
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty(
            "errors",
            ex.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("%s: %s (rejected value: '%s')", error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .toList()
        );
        return problemDetail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(final AccessDeniedException ex, final WebRequest request) {
        logger.warn("Access denied: {}", ex.getMessage());
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_BASE_URL + "forbidden"));
        problemDetail.setTitle("Forbidden");
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(final DomainException ex, final WebRequest request) {
        logger.error("Domain exception occurred: {}", ex.getMessage());
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setType(URI.create(PROBLEM_BASE_URL + "domain-error"));
        problemDetail.setTitle("Domain Error");
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(final Exception ex, final WebRequest request) {
        logger.error("Unexpected error occurred", ex);
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please contact support if the problem persists."
        );
        problemDetail.setType(URI.create(PROBLEM_BASE_URL + "internal-error"));
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
