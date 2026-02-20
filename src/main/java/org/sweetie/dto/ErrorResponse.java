package org.sweetie.dto;

import java.time.Instant;

public record ErrorResponse(
        int status,         // HTTP status code
        String errorCode,   // Application-level error code
        String message,     // Human-readable message
        Instant timestamp   // Optional: when the error occurred
) {}
