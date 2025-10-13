package org.example.thuctap.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, String> fieldErrors;

    public ErrorResponse() {}

    public ErrorResponse(boolean success, String message, LocalDateTime timestamp, String path, Map<String, String> fieldErrors) {
        this.success = success;
        this.message = message;
        this.timestamp = timestamp;
        this.path = path;
        this.fieldErrors = fieldErrors;
    }

    // getters / setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; }
}
