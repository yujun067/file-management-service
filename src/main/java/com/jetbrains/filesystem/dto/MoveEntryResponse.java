package com.jetbrains.filesystem.dto;

public class MoveEntryResponse {
    private String sourcePath;
    private String targetPath;

    public MoveEntryResponse(String sourcePath, String targetPath) {
        this.sourcePath = sourcePath;
        this.targetPath = targetPath;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }


    @Override
    public String toString() {
        return "MoveEntryResponse{" +
                "sourcePath='" + sourcePath + '\'' +
                ", targetPath='" + targetPath +
                '}';
    }
}
