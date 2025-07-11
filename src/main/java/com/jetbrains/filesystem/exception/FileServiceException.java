package com.jetbrains.filesystem.exception;

public abstract class FileServiceException extends RuntimeException {
    public FileServiceException(String msg) { super(msg); }

    public FileServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}