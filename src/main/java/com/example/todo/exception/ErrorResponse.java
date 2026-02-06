package com.example.todo.exception;

import java.time.OffsetDateTime;

public class ErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private String path;
    private OffsetDateTime timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(boolean success, String message, String errorCode, String path, OffsetDateTime timestamp) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.path = path;
        this.timestamp = timestamp;
    }

    public static ErrorResponse of(String message, String errorCode, String path) {
        return new ErrorResponse(false, message, errorCode, path, OffsetDateTime.now());
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
