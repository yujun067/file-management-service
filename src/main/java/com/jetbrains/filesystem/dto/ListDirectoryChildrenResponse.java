package com.jetbrains.filesystem.dto;

import java.util.List;

public class ListDirectoryChildrenResponse {
    private List<FileInfo> children;

    public List<FileInfo> getChildren() {
        return children;
    }

    public void setChildren(List<FileInfo> children) {
        this.children = children;
    }
}
