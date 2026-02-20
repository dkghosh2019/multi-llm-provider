package org.sweetie.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.sweetie.dto.ErrorResponse;

import java.time.Instant;

/**
 * Global exception handler for all REST controllers.
 *
 * <p>Maps exceptions to structured JSON responses with appropriate HTTP status codes.</p>
 *
 * <p>Exception Mapping:</p>
 * <table>
 *     <tr><th>Exception</th><th>HTTP Status</th><th>Error Code</th><th>Message</th></tr>
 *     <tr><td>AIServiceException</td><td>503</td><td>AI_SERVICE_UNAVAILABLE</td><td>AI service is unavailable</td></tr>
 *     <tr><td>MethodArgumentNotValidException</td><td>400</td><td>VALIDATION_FAILED</td><td>Field validation errors</td></tr>
 *     <tr><td>ConstraintViolationException</td><td>400</td><td>CONSTRAINT_VIOLATION</td><td>Request parameter validation errors</td></tr>
 *     <tr><td>IllegalArgumentException</td><td>400</td><td>BAD_REQUEST</td><td>Invalid arguments provided</td></tr>
 *     <tr><td>Other Exceptions</td><td>500</td><td>INTERNAL_SERVER_ERROR</td><td>An unexpected error occurred</td></tr>
 * </table>
 *
 * @see org.sweetie.dto.ErrorResponse
 * @see org.sweetie.exception.AIServiceException
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ----------------------------------------
    // Exception Handlers
    // ----------------------------------------

    /**
     * Handles IllegalArgumentException thrown by controllers or services.
     *
     * @param ex the exception
     * @return structured ErrorResponse with HTTP 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        log.error("IllegalArgumentException caught: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    /**
     * Handles AIServiceException when AI service is unavailable.
     *
     * @param ex the AIServiceException
     * @return structured ErrorResponse with HTTP 503 status
     */
    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ErrorResponse> handleAIServiceException(AIServiceException ex) {
        log.error("AIServiceException caught: {}", ex.getMessage(), ex);
        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                ex.getErrorCode(),
                ex.getMessage()
        );
    }

    /**
     * Handles validation errors for @Valid annotated request bodies (POST/PUT requests).
     *
     * @param ex the exception containing field errors
     * @return structured ErrorResponse with HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Method argument validation failed: {}", ex.getMessage(), ex);

        // Aggregate field errors into a single message (taking first error for simplicity)
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Invalid input");

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", errorMessage);
    }

    /**
     * Handles validation errors for request parameters or path variables violating constraints
     * such as @NotBlank, @Min, @Max. Typically used in GET requests or @Validated controllers.
     *
     * @param ex the ConstraintViolationException
     * @return structured ErrorResponse with HTTP 400 status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleParamValidation(ConstraintViolationException ex) {
        log.error("Constraint violation detected: {}", ex.getMessage(), ex);

        // Combine all violations into a single message
        String message = ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .reduce((s1, s2) -> s1 + "; " + s2)
                .orElse("Invalid request parameters");

        return buildErrorResponse(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", message);
    }

    /**
     * Generic fallback handler for uncaught exceptions.
     * Returns 500 Internal Server Error for unexpected issues.
     *
     * @param ex the exception
     * @return structured ErrorResponse with HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception caught: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", ex.getMessage());
    }

    // ----------------------------------------
    // Private Helper
    // ----------------------------------------

    /**
     * Helper method to build ErrorResponse and ResponseEntity.
     *
     * @param status HTTP status code
     * @param code application-specific error code
     * @param message human-readable error message
     * @return ResponseEntity containing structured ErrorResponse
     */
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String code, String message) {
        ErrorResponse error = new ErrorResponse(
                status.value(),
                code,
                message,
                Instant.now()
        );
        return ResponseEntity.status(status).body(error);
    }
}