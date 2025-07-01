package com.jetbrains.filesystem.service;

import com.jetbrains.filesystem.config.FileServiceProperties;
import com.jetbrains.filesystem.dto.FileInfo;
import com.jetbrains.filesystem.dto.GetFileInfoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileManageService {
    @Autowired
    private FileServiceProperties properties;

    public GetFileInfoResponse getFileInfo(String relativePath) throws IOException {
        Path root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
        Path target = root.resolve(relativePath).normalize();

        File file = target.toFile();
        if (!file.exists()) {
            throw new FileNotFoundException("File not found:"+file.getAbsolutePath());
        }

        GetFileInfoResponse response = new GetFileInfoResponse();
        response.setName(file.getName());
        response.setPath(relativePath);
        response.setSize(file.length());
        return response;
    }

    public List<FileInfo> listDirectoryChildren(String relativePath) throws IOException {
        Path root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
        Path targetDir = root.resolve(relativePath).normalize();

        File dir = targetDir.toFile();
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Path is not a directory:"+ relativePath);
        }

        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("Empty directory children:" + relativePath);
        }

        List<FileInfo> fileInfos = new ArrayList<>();
        for (File file : files) {
            FileInfo info = new FileInfo();
            info.setName(file.getName());
            info.setPath(relativePath+"/"+file.getName());
            info.setSize(file.length());
            info.setDirectory(file.isDirectory());
            fileInfos.add(info);
        }

        return fileInfos;
    }


}
