package com.jetbrains.filesystem.dto;

public class CreateEntryResponse {
    private String name;
    private String path;
    private long size;
    private boolean directory;

    public CreateEntryResponse(String name, String path, long size, boolean directory) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.directory = directory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    @Override
    public String toString() {
        return "CreateEntryResponse{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", directory=" + directory +
                '}';
    }
}
