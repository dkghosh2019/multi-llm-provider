package org.sweetie.aichat.exception;

public class AIServiceException extends RuntimeException {

    private final String errorCode;

    public AIServiceException(String message) {
        super(message);
        this.errorCode = "AI_SERVICE_UNAVAILABLE";
    }

    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AI_SERVICE_UNAVAILABLE";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
