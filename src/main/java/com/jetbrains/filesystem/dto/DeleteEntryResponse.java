package com.jetbrains.filesystem.dto;

public class DeleteEntryResponse {
    private boolean success;
    private String path;

    public DeleteEntryResponse(boolean success, String path) {
        this.success = success;
        this.path = path;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    @Override
    public String toString() {
        return "DeleteEntryResponse{" +
                "success=" + success +
                ", path='" + path + '\'' +
                '}';
    }
}
