package com.jetbrains.filesystem.dto.file;

import com.jetbrains.filesystem.exception.ValidationException;

public enum EntryType {
    FILE, FOLDER;

    public static EntryType fromString(String type) {
        if (type == null) {
            throw new ValidationException("Type must not be null");
        }
        switch (type.toLowerCase()) {
            case "file":
                return FILE;
            case "folder":
                return FOLDER;
            default:
                throw new ValidationException("Unknown type: " + type);
        }
    }}
