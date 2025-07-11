package com.jetbrains.filesystem.service;

import com.jetbrains.filesystem.api.FileManager;
import com.jetbrains.filesystem.dto.file.*;
import com.jetbrains.filesystem.exception.ValidationException;
import com.jetbrains.filesystem.storage.FileStorage;
import com.jetbrains.filesystem.util.FileUtil;
import com.jetbrains.filesystem.util.PathValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.Base64;
import java.util.List;

@Service("localFileManager")
@RequiredArgsConstructor
@Log4j2
public class LocalFileManager implements FileManager {
    private final FileStorage storage;
    private final PathValidator validator;

    public GetFileInfoResponse getFileInfo(String relativePath) {
        Path absPath = validator.toAbsolute(relativePath);
        FileInfo fileInfo = storage.getFileInfo(absPath);

        GetFileInfoResponse response = new GetFileInfoResponse();
        response.setName(fileInfo.getName());
        response.setPath(validator.toRelative(fileInfo.getPath()));
        response.setSize(fileInfo.getSize());
        response.setDirectory(fileInfo.isDirectory());
        log.debug("getFileInfo finished: {}", response.toString());
        return response;
    }

    public ListDirectoryResponse listDirectoryChildren(String relativePath) {
        Path path = validator.toAbsolute(relativePath);
        List<FileInfo> fileInfos = storage.getFileInfolist(path);
        for (FileInfo fileInfo : fileInfos) {
            fileInfo.setPath(validator.toRelative(fileInfo.getPath()));
        }
        ListDirectoryResponse response = new ListDirectoryResponse(fileInfos);
        return response;
    }

    public CreateEntryResponse createEntry(String relativePath, String inputType) {
        EntryType entryType = EntryType.fromString(inputType);
        Path p = validator.toAbsolute(relativePath);
        File targetFile = p.toFile();
        if(targetFile.exists()){
            throw new ValidationException("File or folder already exists:"+ targetFile.getAbsolutePath());
        }

        storage.create(p, entryType);
        CreateEntryResponse response = new CreateEntryResponse(targetFile.getName(),relativePath, targetFile.length(),targetFile.isDirectory());
        return response;
    }

    public DeleteEntryResponse deleteEntry(String relativePath) {
        Path path = validator.toAbsolute(relativePath);
        storage.delete(path);
        DeleteEntryResponse response = new DeleteEntryResponse(relativePath);
        return response;
    }

    public MoveEntryResponse moveEntry(String sourcePath, String targetPath) {
        Path source = validator.toAbsolute(sourcePath);
        Path target = validator.toAbsolute(targetPath);
        validator.validatePath(source, target);

        storage.move(source, target);
        MoveEntryResponse response = new MoveEntryResponse(sourcePath, targetPath);
        return response;
    }

    public CopyEntryResponse copyEntry(String sourcePath, String targetPath) {
        Path source = validator.toAbsolute(sourcePath);
        Path target = validator.toAbsolute(targetPath);
        validator.validatePath(source, target);

        storage.copy(source, target);
        CopyEntryResponse response = new CopyEntryResponse(sourcePath, targetPath);
        return response;
    }

    public ReadFileSegmentResponse readFile(String relativePath, long offset, int length)  {
        Path path = validator.toAbsolute(relativePath);
        ByteBuffer buffer = storage.read(path, offset, length);
        String base64Data = FileUtil.encodeBase64(buffer);
        ReadFileSegmentResponse response = new ReadFileSegmentResponse(base64Data);
        return response;
    }

    public AppendDataToFileResponse appendDataToFile(String relativePath, String encodedData)  {
        Path source = validator.toAbsolute(relativePath);
        validator.validateSourceForAppend(source);

        byte[] originalData = null;
        try {
            originalData = Base64.getDecoder().decode(encodedData);
        } catch (Exception e) {
            throw new ValidationException("Invalid encoded data");
        }

        int appendLength = storage.append(source, originalData);
        AppendDataToFileResponse response = new AppendDataToFileResponse(relativePath, appendLength);
        return response;
    }

}







