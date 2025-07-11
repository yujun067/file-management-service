package com.jetbrains.filesystem.exception;

public class FileOperationException extends FileServiceException{
    public FileOperationException(String message) {
        super(message);
    }
    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
