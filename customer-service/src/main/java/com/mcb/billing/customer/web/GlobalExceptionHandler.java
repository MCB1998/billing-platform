package com.mcb.billing.customer.web;

import com.mcb.billing.customer.exception.CustomerNotFoundException;
import com.mcb.billing.customer.exception.DuplicateEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Central exception handling for all controllers. Translates domain exceptions
 * into proper HTTP status codes and RFC 7807 {@link ProblemDetail} response bodies
 * (content type {@code application/problem+json}). Returning a ProblemDetail lets
 * Spring set the HTTP status from it automatically.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Unknown customer number -> 404 Not Found. */
    @ExceptionHandler(CustomerNotFoundException.class)
    public ProblemDetail handleNotFound(CustomerNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Customer not found");
        return problem;
    }

    /** Email already used by another customer -> 409 Conflict. */
    @ExceptionHandler(DuplicateEmailException.class)
    public ProblemDetail handleDuplicateEmail(DuplicateEmailException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Email already in use");
        return problem;
    }

    /** Bean Validation failure on a request body -> 400 with per-field messages. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setTitle("Invalid request");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        problem.setProperty("errors", errors);

        return problem;
    }
}
