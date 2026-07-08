package com.mcb.billing.invoice.web;

import com.mcb.billing.invoice.exception.CustomerServiceUnavailableException;
import com.mcb.billing.invoice.exception.InactiveCustomerException;
import com.mcb.billing.invoice.exception.InvalidInvoiceStateException;
import com.mcb.billing.invoice.exception.InvoiceNotFoundException;
import com.mcb.billing.invoice.exception.UnknownCustomerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Central exception handling. Translates domain exceptions into HTTP status codes
 * and RFC 7807 {@link ProblemDetail} response bodies.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Unknown invoice number -> 404. */
    @ExceptionHandler(InvoiceNotFoundException.class)
    public ProblemDetail handleInvoiceNotFound(InvoiceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Invoice not found");
        return problem;
    }

    /** Invoice references a non-existent customer -> 422. */
    @ExceptionHandler(UnknownCustomerException.class)
    public ProblemDetail handleUnknownCustomer(UnknownCustomerException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Unknown customer");
        return problem;
    }

    /** Invoice references an inactive customer -> 422. */
    @ExceptionHandler(InactiveCustomerException.class)
    public ProblemDetail handleInactiveCustomer(InactiveCustomerException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setTitle("Customer inactive");
        return problem;
    }

    /** Illegal status transition (e.g. issuing a non-DRAFT invoice) -> 409. */
    @ExceptionHandler(InvalidInvoiceStateException.class)
    public ProblemDetail handleInvalidState(InvalidInvoiceStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Invalid invoice state");
        return problem;
    }

    /** customer-service unreachable/failing -> 503. */
    @ExceptionHandler(CustomerServiceUnavailableException.class)
    public ProblemDetail handleUnavailable(CustomerServiceUnavailableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        problem.setTitle("Customer service unavailable");
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
