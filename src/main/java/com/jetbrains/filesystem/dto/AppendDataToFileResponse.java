package com.jetbrains.filesystem.dto;

public class AppendDataToFileResponse {
    private String path;
    private int appendLength;

    public AppendDataToFileResponse(String path, int appendLength) {
        this.path = path;
        this.appendLength = appendLength;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getAppendLength() {
        return appendLength;
    }

    public void setAppendLength(int appendLength) {
        this.appendLength = appendLength;
    }
}
