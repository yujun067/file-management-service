package com.jetbrains.filesystem.service;

import com.jetbrains.filesystem.FilesystemApplication;
import com.jetbrains.filesystem.config.FileServiceProperties;
import com.jetbrains.filesystem.dto.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.util.StringUtils;

@Service
public class FileManageService {
    private static final Logger logger = LogManager.getLogger(FilesystemApplication.class);

    private FileServiceProperties properties;

    public FileManageService(FileServiceProperties properties) {
        this.properties = properties;
    }

    public GetFileInfoResponse getFileInfo(String relativePath) throws IOException {
        if(relativePath == null || relativePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid path");
        }
        Path root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
        Path target = root.resolve(relativePath).normalize();

        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Invalid path: Outside root folder");
        }
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
        if(relativePath == null || relativePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid path");
        }
        Path root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
        Path targetDir = root.resolve(relativePath).normalize();

        if (!targetDir.startsWith(root)) {
            throw new IllegalArgumentException("Invalid path: Outside root folder");
        }
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

    public CreateEntryResponse createEntry(String relativePath, String type) throws IOException {
        Path root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
        Path target = root.resolve(relativePath).normalize();

        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Invalid path: Outside root folder");
        }
        File targetFile = target.toFile();
        if(targetFile.exists()){
            throw new IllegalArgumentException("File or folder already exists:"+ targetFile.getAbsolutePath());
        }

        try {
            if ("file".equals(type)) {
                Files.createDirectories(target.getParent());
                Files.createFile(target);
            } else if("folder".equals(type)) {
                Files.createDirectories(target);
            } else {
                throw new IllegalArgumentException("type must be 'file' or 'folder'");
            }
        } catch (IOException e) {
            throw new IOException("Failed to create path: " + e.getMessage());
        }

        CreateEntryResponse response = new CreateEntryResponse(targetFile.getName(),relativePath, targetFile.length(),targetFile.isDirectory());
        return response;
    }

    public DeleteEntryResponse deleteEntry(String relativePath) throws IOException {
        Path root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
        Path target = root.resolve(relativePath).normalize();

        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Invalid path: Outside root folder");
        }
        if(!Files.exists(target)){
            throw new FileNotFoundException("File not found:"+relativePath);
        }

        try {
            Files.walkFileTree(target, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    deletePath(file);
                    logger.debug("delete file:{}", file.toString());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    deletePath(dir);
                    logger.debug("delete dir:{}", dir.toString());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new IOException("Failed to delete: " + e.getMessage(), e);
        }

        DeleteEntryResponse response = new DeleteEntryResponse(relativePath);
        return response;
    }

    //Encapsulate as testable methods
    public void deletePath(Path path) throws IOException {
        Files.delete(path);
    }

    public MoveEntryResponse moveEntry(String sourcePath, String targetPath) throws IOException {
        Path root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
        Path source = root.resolve(sourcePath).normalize();
        Path target = root.resolve(targetPath).normalize();

        validatePath(source, target, root);

        //prevent self-contain
        if (target.startsWith(source)) {
            throw new IllegalArgumentException("own subdirectories");
        }

        //ensure the target's parent folder exists
        Files.createDirectories(target.getParent());

        try {
            movePath(source, target);
        } catch (IOException e) {
            throw new IOException("Failed to move file:" + sourcePath + " to target:" + targetPath);
        }

        MoveEntryResponse response = new MoveEntryResponse(sourcePath, targetPath);
        return response;
    }
    //Encapsulate as testable methods
    public void movePath(Path source, Path target) throws IOException {
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }


    public CopyEntryResponse copyEntry(String sourcePath, String targetPath) throws IOException {
        Path root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
        Path source = root.resolve(sourcePath).normalize();
        Path target = root.resolve(targetPath).normalize();

        //prevent self-contain
        if (target.startsWith(source)) {
            throw new IllegalArgumentException("own subdirectories");
        }

        validatePath(source, target, root);

        //ensure the target's parent folder exists
        Files.createDirectories(target.getParent());

        try {
            copyPath(source, target);
        } catch (IOException e) {
            throw new IOException("Failed to copy file:" + sourcePath + " to target:" + targetPath);
        }

        CopyEntryResponse response = new CopyEntryResponse(sourcePath, targetPath);
        return response;
    }

    public void copyPath(Path source, Path target) throws IOException{
        if(Files.isDirectory(source)) {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetDir = target.resolve(source.relativize(dir));
                    Files.createDirectories(targetDir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetFile = target.resolve(source.relativize(file));
                    Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void validatePath(Path source, Path target, Path root ) throws IOException {
        if (!source.startsWith(root)||!target.startsWith(root)) {
            throw new IllegalArgumentException("Invalid path: Outside root folder");
        }
        if(!Files.exists(source)){
            throw new FileNotFoundException("source not found:"+source.toString());
        }
        if(Files.exists(target)){
           throw new IllegalArgumentException("target already exists:"+target.toString());
        }
    }

    public ReadFileSegmentResponse readFile(String relativePath, long offset, int length) throws IOException {
        Path root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
        Path source = root.resolve(relativePath).normalize();
        if (!source.startsWith(root)) {
            throw new IllegalArgumentException("Invalid path: Outside root folder");
        }

        File sourceFile = source.toFile();
        if(length<=0) {
            throw new IllegalArgumentException("length must be positive");
        }
        if(offset < 0 || offset > sourceFile.length()){
            throw new IllegalArgumentException("offset must be in range [0,"+sourceFile.length()+")");
        }
        if(!sourceFile.exists() || !sourceFile.isFile()){
            throw new FileNotFoundException("File not found:"+relativePath);
        }


        byte[] buffer = new byte[length];
        int bytesRead = 0;

        try(RandomAccessFile raf = new RandomAccessFile(sourceFile, "r")){
            raf.seek(offset);
            bytesRead = raf.read(buffer,0, length);
        }

        if(bytesRead == -1){
            bytesRead = 0;
        }

        String base64Data = Base64.getEncoder().encodeToString(Arrays.copyOf(buffer,bytesRead));
        ReadFileSegmentResponse response = new ReadFileSegmentResponse(base64Data);
        return response;
    }


    private final Cache<String, ReentrantLock> fileLocks = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)  // auto clean unused lock
            .build();

    private ReentrantLock getLockForFile(String path) {
        return fileLocks.get(path, k -> new ReentrantLock());
    }

    public AppendDataToFileResponse appendDataToFile(String relativePath, String encodedData) throws IOException {
        Path root = Paths.get(properties.getRootFolder()).toAbsolutePath().normalize();
        Path source = root.resolve(relativePath).normalize();

        if (!source.startsWith(root)) {
            throw new IllegalArgumentException("Invalid path: Outside root folder");
        }
        File sourceFile = source.toFile();
        if(!sourceFile.exists()){
            throw new FileNotFoundException("File not found:"+relativePath);
        }
        if(!sourceFile.isFile()){
            throw new IllegalArgumentException("Can't append to a directory:"+relativePath);
        }

        int appendLength = appendFileContent(source, sourceFile, encodedData);
        AppendDataToFileResponse response = new AppendDataToFileResponse(relativePath, appendLength);
        return response;
    }

    public int appendFileContent(Path source, File sourceFile, String encodedData) throws  IOException {
        ReentrantLock lock = getLockForFile(source.toString());
        int appendLength = 0;
        lock.lock();
        try(FileOutputStream fos = new FileOutputStream(sourceFile, true)) {
            byte[] originalData = Base64.getDecoder().decode(encodedData);
            fos.write(originalData);
            appendLength = originalData.length;
        } catch(IllegalArgumentException exception) {
            throw new IllegalArgumentException("failed to append file content");
        }
        finally {
            lock.unlock();
        }

        return appendLength;
    }

}














