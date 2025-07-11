package com.jetbrains.filesystem.storage;

import com.jetbrains.filesystem.dto.file.EntryType;
import com.jetbrains.filesystem.dto.file.FileInfo;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

public interface FileStorage {
    FileInfo getFileInfo(Path path);
    List<FileInfo> getFileInfolist(Path path);
    void create(Path path, EntryType type);
    void delete(Path path);
    void move(Path source, Path target);
    void copy(Path source, Path target);
    ByteBuffer read(Path source, long offset, int length);
    int append(Path absPath, byte[] data);
}
