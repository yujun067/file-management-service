package com.jetbrains.filesystem.exception;

public class MethodNotFoundException extends FileServiceException {
    public MethodNotFoundException(String message) {
        super(message);
    }

    public MethodNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
