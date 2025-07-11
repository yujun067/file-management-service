package com.jetbrains.filesystem.api;

import com.jetbrains.filesystem.dto.file.*;

public interface FileManager {
    GetFileInfoResponse getFileInfo(String relativePath);
    ListDirectoryResponse listDirectoryChildren(String relativePath);
    CreateEntryResponse createEntry(String relativePath, String type);
    DeleteEntryResponse deleteEntry(String relativePath);
    MoveEntryResponse moveEntry(String sourcePath, String targetPath);
    CopyEntryResponse copyEntry(String sourcePath, String targetPath);
    ReadFileSegmentResponse readFile(String relativePath, long offset, int length);
    AppendDataToFileResponse appendDataToFile(String relativePath, String encodedData);
}
