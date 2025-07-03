package com.jetbrains.filesystem.dto;

public class DeleteEntryResponse {
    private String path;

    public DeleteEntryResponse(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    @Override
    public String toString() {
        return "DeleteEntryResponse{path='" + path + '\'' + '}';
    }
}
