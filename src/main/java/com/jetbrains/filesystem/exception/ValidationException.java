package com.jetbrains.filesystem.exception;

public class ValidationException extends FileServiceException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
