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
 * GlobalExceptionHandler provides centralized exception handling across all REST controllers.
 * It maps exceptions to appropriate HTTP status codes and structured JSON responses.
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
 * <p>Example API Response:</p>
 * <pre>
 * {
 *   "status": 503,
 *   "errorCode": "AI_SERVICE_UNAVAILABLE",
 *   "message": "AI service is unavailable",
 *   "timestamp": "2026-02-19T19:01:25.123Z"
 * }
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles IllegalArgumentException thrown by controllers or services.
     *
     * @param ex the thrown IllegalArgumentException
     * @return structured ErrorResponse with HTTP 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        log.error("IllegalArgumentException caught: {}", ex.getMessage(), ex);

        // Construct error response
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                ex.getMessage(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Generic fallback handler for all uncaught exceptions.
     * Ensures that unexpected errors return a 500 status with a proper JSON response.
     *
     * @param ex the thrown Exception
     * @return structured ErrorResponse with HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception caught: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                ex.getMessage(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    /**
     * Handles validation errors for @Valid annotated request bodies (POST/PUT requests).
     *
     * @param ex the MethodArgumentNotValidException containing field errors
     * @return structured ErrorResponse with HTTP 400 status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Method argument validation failed: {}", ex.getMessage(), ex);

        // Combine all field errors into a single message (taking the first error for simplicity)
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .findFirst()
                .orElse("Invalid input");

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                errorMessage,
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Handles validation errors for request parameters, path variables, or method arguments
     * that violate constraints such as @NotBlank, @Min, @Max.
     * Typically used for GET requests or @Validated controllers.
     *
     * <p>Example:</p>
     * <pre>
     * @GetMapping("/greet")
     * public String greet(@RequestParam @NotBlank String name) {
     *     return "Hello " + name;
     * }
     * </pre>
     *
     * @param ex the ConstraintViolationException thrown by Jakarta Bean Validation
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

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "CONSTRAINT_VIOLATION",
                message,
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    /**
     * Handles AIServiceException thrown when the AI service is unavailable.
     * Frontend can check the errorCode to provide retry logic.
     *
     * <p>Example Frontend:</p>
     * <pre>
     * if (error.errorCode === "AI_SERVICE_UNAVAILABLE") {
     *     showRetryButton();
     * }
     * </pre>
     *
     * @param ex the AIServiceException
     * @return structured ErrorResponse with HTTP 503 status
     */
    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<ErrorResponse> handleAIServiceException(AIServiceException ex) {
        log.error("AIServiceException caught: {}", ex.getMessage(), ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                ex.getErrorCode(),
                ex.getMessage(),
                Instant.now()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(error);
    }
}